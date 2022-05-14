package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.ApplicationInfoResponse;
import hu.psprog.leaflet.lags.acceptance.model.HealthCheckResponse;
import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for management operations related test scenarios.
 *
 * @author Peter Smith
 */
public class ManagementStepDefinition implements En {

    private final LAGSClient lagsClient;

    @Autowired
    public ManagementStepDefinition(LAGSClient lagsClient) {
        this.lagsClient = lagsClient;

        defineActions();
        defineAssertions();
    }

    private void defineActions() {

        When("calling the health check endpoint",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestHealthCheck()));

        When("calling the application info endpoint",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestApplicationInfo()));
    }

    private void defineAssertions() {

        Then("^the application status is ([A-Z]+)$", (String status) -> {

            ResponseEntity<HealthCheckResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getStatus(), equalTo(status));
        });

        Then("^the reported application (name|abbreviation) is (.*)$", (String field, String value) -> {

            ResponseEntity<ApplicationInfoResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(ApplicationInfoResponse.FIELD_MAPPING.get(field).apply(response.getBody()), equalTo(value));
        });
    }
}
