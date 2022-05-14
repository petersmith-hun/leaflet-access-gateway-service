package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step definition implementation for standard user authentication related test scenarios.
 *
 * @author Peter Smith
 */
public class AuthenticationStepDefinition implements En {

    private static final String SUCCESSFUL_RECAPTCHA_KEYWORD = "succeeded";

    private final LAGSClient lagsClient;

    @Autowired
    public AuthenticationStepDefinition(LAGSClient lagsClient) {
        this.lagsClient = lagsClient;

        defineConditions();
        defineActions();
    }

    private void defineConditions() {

        Given("^the user identifies with the email address (.*)$",
                (String email) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.EMAIL, email));

        Given("^the user uses the password (.*)$",
                (String password) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.PASSWORD, password));

        Given("^the user confirms the password is (.*)$",
                (String password) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.PASSWORD_CONFIRM, password));

        Given("^the user picks the username (.*)$",
                (String username) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.USERNAME, username));

        Given("^ReCaptcha verification (succeeded|failed)$", (String verificationStatus) -> {

            ThreadLocalDataRegistry.put(TestConstants.Attribute.RECAPTCHA_TOKEN, SUCCESSFUL_RECAPTCHA_KEYWORD.equals(verificationStatus)
                    ? TestConstants.Attribute.RECAPTCHA_TOKEN.getValue()
                    : "invalidToken");
            ThreadLocalDataRegistry.putFlag(TestConstants.Flag.USE_RECAPTCHA_VERIFICATION);
        });
    }

    private void defineActions() {

        When("^the user signs in$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestLogin()));

        When("^the user signs up$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestSignUp()));

        When("^the user signs out$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestSignOut()));
    }
}
