package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for commonly used test steps.
 *
 * @author Peter Smith
 */
@Slf4j
public class CommonStepDefinition implements En {

    public CommonStepDefinition() {

        defineCommonSteps();
        defineAssertions();
    }

    private void defineCommonSteps() {

        Before(scenario -> log.info("Starting scenario '{}'...", scenario.getName()));
        After(scenario -> log.info("Scenario '{}' completed with status {}", scenario.getName(), scenario.getStatus()));

        After(ThreadLocalDataRegistry::reset);
    }

    private void defineAssertions() {

        Then("^the application responds with HTTP status ([A-Z_]+)$", (HttpStatus httpStatus) -> {

            ResponseEntity<Void> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getStatusCode(), equalTo(httpStatus));
        });

        Then("^the user is redirected to (/.*)", (String location) -> {

            ResponseEntity<Void> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(String.valueOf(response.getHeaders().getLocation()), equalTo(location));
        });

        Then("^the user receives the ([A-Z_]+) mail$", (TestConstants.Attribute emailAttribute) -> {

            Object mailObject = ThreadLocalDataRegistry.get(emailAttribute);
            assertThat(mailObject, notNullValue());
        });

        Then("^the user does not receive the ([A-Z_]+) mail$", (TestConstants.Attribute emailAttribute) -> {

            Object mailObject = ThreadLocalDataRegistry.get(emailAttribute);
            assertThat(mailObject, nullValue());
        });

        Then("^the response body contains \"(.*)\"$", (String bodyContent) -> {

            ResponseEntity<String> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().contains(bodyContent), is(true));
        });
    }
}
