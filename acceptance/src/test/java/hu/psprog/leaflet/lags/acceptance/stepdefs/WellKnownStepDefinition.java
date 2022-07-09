package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for well-known endpoints related tests.
 *
 * @author Peter Smith
 */
public class WellKnownStepDefinition implements En {

    private final LAGSClient lagsClient;

    @Autowired
    public WellKnownStepDefinition(LAGSClient lagsClient) {
        this.lagsClient = lagsClient;

        defineActions();
        defineAssertions();
    }

    private void defineActions() {

        When("^the JWKs endpoint is called$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestJWKs()));

        When("^the server meta-info endpoint is called$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestServerMetaInfo()));
    }

    private void defineAssertions() {

        Then("^the JWKs response contains one entry$", () -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(((List<?>) response.getBody().get("keys")).size(), equalTo(1));
        });

        Then("^the JWKs response contains the public ([A-Z]+) key with ([A-Z0-9]+) algorithm for ([a-z]+) verification$", (String kty, String alg, String use) -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());

            List<Map<String, String>> jwkList = (List<Map<String, String>>) response.getBody().get("keys");
            Map<String, String> publicKey = jwkList.get(0);
            assertThat(publicKey.get("kty"), equalTo(kty));
            assertThat(publicKey.get("alg"), equalTo(alg));
            assertThat(publicKey.get("use"), equalTo(use.substring(0, 3)));
            assertThat(publicKey.get("n"), notNullValue());
        });

        Then("^the JWKs response contains the key id with the value of (.*)$", (String kid) -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());

            List<Map<String, String>> jwkList = (List<Map<String, String>>) response.getBody().get("keys");
            Map<String, String> publicKey = jwkList.get(0);
            assertThat(publicKey.get("kid"), equalTo(kid));
        });

        Then("^the meta-info response contains the key ([a-z_]+) with value (.*)$", (String key, String value) -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().get(key), equalTo(value));
        });

        Then("^the meta-info response contains the key ([a-z_]+) with values (.*)$", (String key, String value) -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().get(key), equalTo(List.of(value.split(","))));
        });
    }
}
