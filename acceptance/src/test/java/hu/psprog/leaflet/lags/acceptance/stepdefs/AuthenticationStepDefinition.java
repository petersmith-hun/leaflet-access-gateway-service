package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.stub.ExternalAuthenticationMock;
import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for standard user authentication related test scenarios.
 *
 * @author Peter Smith
 */
public class AuthenticationStepDefinition implements En {

    private static final String SUCCESSFUL_RECAPTCHA_KEYWORD = "succeeded";

    private final LAGSClient lagsClient;
    private final ExternalAuthenticationMock externalAuthenticationMock;

    @Autowired
    public AuthenticationStepDefinition(LAGSClient lagsClient, ExternalAuthenticationMock externalAuthenticationMock) {
        this.lagsClient = lagsClient;
        this.externalAuthenticationMock = externalAuthenticationMock;

        defineConditions();
        defineActions();
        defineAssertions();
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

        Given("^the external user uses the email address (.*)$",
                (String email) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.EMAIL, email));
    }

    private void defineActions() {

        When("^the user signs in$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestLogin()));

        When("^the user signs up$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestSignUp()));

        When("^the user signs out$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestSignOut()));

        When("^the user requests sign-in via (GitHub)$", (String provider) -> {

            ExternalAuthenticationMock.Provider resolvedProvider = ExternalAuthenticationMock.Provider.valueOf(provider.toUpperCase());
            ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestExternalLogin(resolvedProvider));
        });

        When("^the user authorizes access on (GitHub)$", (String provider) -> {

            ExternalAuthenticationMock.Provider resolvedProvider = ExternalAuthenticationMock.Provider.valueOf(provider.toUpperCase());
            externalAuthenticationMock.registerProviderMock(resolvedProvider);
            ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestExternalLoginReturn(resolvedProvider, true));
            externalAuthenticationMock.resetProviderMock();
        });

        When("^the user rejects access on (GitHub)$", (String provider) -> {

            ExternalAuthenticationMock.Provider resolvedProvider = ExternalAuthenticationMock.Provider.valueOf(provider.toUpperCase());
            externalAuthenticationMock.registerProviderMock(resolvedProvider);
            ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestExternalLoginReturn(resolvedProvider, false));
            externalAuthenticationMock.resetProviderMock();
        });

        When("^the user is returned to the authorization page$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestAuthorizationPage()));
    }

    private void defineAssertions() {

        Then("^the external user is logged in as (.*)$", (String username) -> {

            String usernameSection = String.format("<span style=\"font-weight: bold;\">%s</span>", username);
            ResponseEntity<String> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().contains(usernameSection), is(true));
        });

        Then("^the external user is logged in with the email address (.*)$", (String email) -> {

            String emailSection = String.format("<p>(<span>%s</span>)</p>", email);
            ResponseEntity<String> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().contains(emailSection), is(true));
        });

        Then("^the external user has the scopes ([a-z: ]+)$", (String scope) -> {

            ResponseEntity<String> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            List<String> scopes = List.of(scope.split(StringUtils.SPACE));
            assertThat(StringUtils.countMatches(response.getBody(), "??form.authorize.scope."), equalTo(scopes.size()));
            scopes.forEach(scopeItem -> assertThat(response.getBody().contains(String.format("??form.authorize.scope.%s_hu_HU??", scopeItem)), is(true)));
        });
    }
}
