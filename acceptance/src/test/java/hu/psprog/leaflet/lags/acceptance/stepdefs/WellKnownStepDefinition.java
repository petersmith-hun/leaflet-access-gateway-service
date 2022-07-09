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
    }

    private void defineAssertions() {

        Then("^the response contains one entry$", () -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(((List<?>) response.getBody().get("keys")).size(), equalTo(1));
        });

        Then("^the response contains the public RSA key for signature verification$", () -> {

            ResponseEntity<HashMap<String, Object>> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());

            List<Map<String, String>> jwkList = (List<Map<String, String>>) response.getBody().get("keys");
            Map<String, String> publicKey = jwkList.get(0);
            assertThat(publicKey.get("kty"), equalTo("RSA"));
            assertThat(publicKey.get("use"), equalTo("sig"));
            assertThat(publicKey.get("alg"), equalTo("RS256"));
            assertThat(publicKey.get("kid"), equalTo("acceptance-test-public-key"));
            assertThat(publicKey.get("n"), notNullValue());
        });
    }
}
