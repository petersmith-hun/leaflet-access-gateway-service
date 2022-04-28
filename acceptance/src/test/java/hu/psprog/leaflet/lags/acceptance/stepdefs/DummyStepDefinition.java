package hu.psprog.leaflet.lags.acceptance.stepdefs;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Dummy step definition for testing purposes.
 *
 * @author Peter Smith
 */
public class DummyStepDefinition {

    @Autowired
    private RestTemplate restTemplate;

    private ResponseEntity<HashMap> response;

    @When("calling the health check endpoint")
    public void callingTheHealthCheckEndpoint() {
        response = restTemplate.getForEntity("http://localhost:9085/lags/actuator/health", HashMap.class);
    }

    @Then("^application responds with HTTP status ([A-Z_]+)$")
    public void applicationShouldRespondWithHTTPStatusOK(HttpStatus expectedStatus) {
        assertThat(response.getStatusCode(), equalTo(expectedStatus));
    }
}
