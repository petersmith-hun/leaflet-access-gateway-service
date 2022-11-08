package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.notification.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import io.cucumber.java8.En;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.RECLAIM_AUTHORITY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for password reset related test scenarios.
 *
 * @author Peter Smith
 */
public class PasswordResetStepDefinition implements En {

    private final LAGSClient lagsClient;
    private final TokenHandler tokenHandler;

    @Autowired
    public PasswordResetStepDefinition(LAGSClient lagsClient, TokenHandler tokenHandler) {
        this.lagsClient = lagsClient;
        this.tokenHandler = tokenHandler;

        defineConditions();
        defineActions();
        defineAssertions();
    }

    private void defineConditions() {

        Given("^the user uses the link in the mail to navigate to the confirmation form$", () -> {

            PasswordResetRequest passwordResetRequest = ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL);
            ThreadLocalDataRegistry.put(TestConstants.Attribute.TOKEN, passwordResetRequest.getToken());
        });

        Given("^the user is authorized with the reset token$", () -> {

            PasswordResetRequest passwordResetRequest = ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL);
            ThreadLocalDataRegistry.put(TestConstants.Attribute.USER_AUTH, passwordResetRequest.getToken());
        });
    }

    private void defineActions() {

        When("^the user requests password reset$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestPasswordReset()));

        When("^the user confirms the password reset$",
                () -> ThreadLocalDataRegistry.putResponseEntity(lagsClient.requestPasswordResetConfirmation()));
    }

    private void defineAssertions() {

        Then("^the UI notifies the user about the accepted password reset request$", () -> {

            ResponseEntity<String> passwordResetNotification = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(passwordResetNotification.getBody(), notNullValue());
            assertThat(passwordResetNotification.getBody().contains("flash.pwreset.request.ack"), is(true));
        });

        Then("^the mail contains a reset token$", () -> {

            PasswordResetRequest passwordResetRequest = ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL);
            assertThat(passwordResetRequest.getToken(), notNullValue());
        });

        Then("^the reset token expires in (\\d+) minutes$", (Integer expiration) -> {

            PasswordResetRequest passwordResetRequest = ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL);
            assertThat(passwordResetRequest.getExpiration(), equalTo(expiration));
        });

        Then("^the reset token scope is limited write:reclaim authority$", () -> {

            PasswordResetRequest passwordResetRequest = ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD_RESET_REQUEST_MAIL);
            TokenClaims claims = tokenHandler.parseToken(passwordResetRequest.getToken());
            assertThat(claims.getScopeAsArray().length, equalTo(1));
            assertThat(claims.getScopeAsArray()[0], equalTo(RECLAIM_AUTHORITY.getAuthority()));
        });
    }
}
