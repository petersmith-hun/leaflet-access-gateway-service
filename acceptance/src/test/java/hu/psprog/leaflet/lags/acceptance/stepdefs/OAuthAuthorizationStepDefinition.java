package hu.psprog.leaflet.lags.acceptance.stepdefs;

import hu.psprog.leaflet.lags.acceptance.model.ErrorResponse;
import hu.psprog.leaflet.lags.acceptance.model.OAuthTokenResponse;
import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.model.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.acceptance.model.UserInfoResponse;
import hu.psprog.leaflet.lags.acceptance.utility.AuthorizationUtility;
import hu.psprog.leaflet.lags.acceptance.utility.HTTPUtility;
import hu.psprog.leaflet.lags.acceptance.utility.LAGSClient;
import hu.psprog.leaflet.lags.acceptance.utility.ThreadLocalDataRegistry;
import io.cucumber.java8.En;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Step definition implementation for OAuth Authorization related test scenarios.
 *
 * @author Peter Smith
 */
@Slf4j
public class OAuthAuthorizationStepDefinition implements En {

    private final LAGSClient lagsClient;

    @Autowired
    public OAuthAuthorizationStepDefinition(LAGSClient lagsClient) {
        this.lagsClient = lagsClient;

        defineConditions();
        defineActions();
        defineAssertions();
    }

    private void defineConditions() {

        Given("^a client identified by ([a-z0-9_]+) tries authorization$",
                (String clientID) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.CLIENT_ID, clientID));

        Given("^the client authenticates with its client secret (.+)$",
                (String clientSecret) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.CLIENT_SECRET, clientSecret));

        Given("^the client requests access to a service identified by the ([a-z:]+) audience$",
                (String audience) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.AUDIENCE, audience));

        Given("^the client chooses ([a-z_]+) grant type$",
                (String grantType) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.GRANT_TYPE, grantType));

        Given("^the client requests access for scope ([a-z: ]+)$",
                (String scope) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.SCOPE, scope));

        Given("^the authorization is requested by application ([a-zA-Z0-9_]+)$",
                (String clientID) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.CLIENT_ID, clientID));

        Given("^the response type is set to ([a-z_]+)$",
                (String responseType) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.RESPONSE_TYPE, responseType));

        Given("^the application requests redirection to (.+)$",
                (String redirectURI) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.REDIRECT_URI, redirectURI));

        Given("^the sent state value is (.+)$",
                (String state) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.STATE, state));

        Given("^the user is signed in as (.+) with password (.*)$",
                (String email, String password) -> ThreadLocalDataRegistry.put(TestConstants.Attribute.USER_AUTH,
                        AuthorizationUtility.generateUserBasicAuthorization(email, password)));

        Given("^the client uses the previously claimed authorization code$",
                () -> ThreadLocalDataRegistry.putFlag(TestConstants.Flag.USE_AUTHORIZATION_CODE));

        Given("^the client uses the specified redirect URI$",
                () -> ThreadLocalDataRegistry.putFlag(TestConstants.Flag.USE_REDIRECT_URI));

        Given("^the client waits for (\\d+) seconds$", (Integer waitInSeconds) -> {

            log.info("Execution blocked for {} seconds...", waitInSeconds);
            Thread.sleep(waitInSeconds * 1000);
            log.info("Execution continues.");
        });

        Given("^the previously issued token is prepared for introspection$", () -> {

            ResponseEntity<OAuthTokenResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            ThreadLocalDataRegistry.put(TestConstants.Attribute.TOKEN, Objects.requireNonNull(response.getBody()).getAccessToken());
        });

        Given("^the client is authorized with the formerly requested token$",
                () -> ThreadLocalDataRegistry.putFlag(TestConstants.Flag.USE_TOKEN_AUTHORIZATION));
    }

    private void defineActions() {

        When("^the client requests a token$", () -> {

            ResponseEntity<OAuthTokenResponse> response = lagsClient.requestToken();
            ThreadLocalDataRegistry.putResponseEntity(response);
        });

        When("^the authorization is requested$", () -> {

            ResponseEntity<String> response = lagsClient.requestAuthorization();
            ThreadLocalDataRegistry.putResponseEntity(response);
            ThreadLocalDataRegistry.put(TestConstants.Attribute.LOCATION, response.getHeaders().getLocation());
        });

        When("^the client requests introspection$", () -> {

            ResponseEntity<TokenIntrospectionResult> response = lagsClient.requestIntrospection();
            ThreadLocalDataRegistry.putResponseEntity(response);
        });

        When("^the client requests userinfo$", () -> {

            ResponseEntity<UserInfoResponse> response = lagsClient.requestUserInfo();
            ThreadLocalDataRegistry.putResponseEntity(response);
        });
    }

    private void defineAssertions() {

        Then("^the response contains a token$", () -> {

            ResponseEntity<OAuthTokenResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getAccessToken(), notNullValue());
        });

        Then("^the returned token is a Bearer type token$", () -> {

            ResponseEntity<OAuthTokenResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getTokenType(), equalTo("Bearer"));
        });

        Then("^the returned token expires in ([0-9]+) seconds$", (Integer seconds) -> {

            ResponseEntity<OAuthTokenResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getExpiresIn(), equalTo(seconds));
        });

        Then("^the returned token gives access to scope ([a-z: ]+)$", (String scope) -> {

            ResponseEntity<OAuthTokenResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getScope(), equalTo(scope));
        });

        Then("^the user is redirected to the specified redirection$", () -> {

            URI location = ThreadLocalDataRegistry.get(TestConstants.Attribute.LOCATION);
            assertThat(location.toString().startsWith(ThreadLocalDataRegistry.get(TestConstants.Attribute.REDIRECT_URI)), is(true));
        });

        Then("^the response contains the authorization code$", () -> {

            URI location = ThreadLocalDataRegistry.get(TestConstants.Attribute.LOCATION);
            String authorizationCode = HTTPUtility.getQueryParameter(location, TestConstants.Attribute.CODE);
            assertThat(authorizationCode, notNullValue());
            ThreadLocalDataRegistry.put(TestConstants.Attribute.CODE, authorizationCode);
        });

        Then("^the response contains the sent state$", () -> {

            URI location = ThreadLocalDataRegistry.get(TestConstants.Attribute.LOCATION);
            assertThat(HTTPUtility.getQueryParameter(location, TestConstants.Attribute.STATE), equalTo(ThreadLocalDataRegistry.get(TestConstants.Attribute.STATE)));
        });

        Then("^the OAuth error code is ([a-zA-Z_]+)$", (String errorCode) -> {

            ResponseEntity<?> errorResponse = ThreadLocalDataRegistry.getResponseEntity();
            if (errorResponse.getBody() instanceof ErrorResponse) {
                assertThat(((ErrorResponse) errorResponse.getBody()).getMessage(), equalTo(errorCode));
            } else if (errorResponse.getBody() instanceof String) {
                assertThat(((String) errorResponse.getBody()).contains(errorCode), is(true));
            } else {
                assertThat(((OAuthTokenResponse) errorResponse.getBody()).getErrorCode(), equalTo(errorCode));
            }
        });

        Then("^the rejection message is (.*)$", (String errorMessage) -> {

            ResponseEntity<?> errorResponse = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(Objects.nonNull(errorResponse.getBody()), is(true));
            if (errorResponse.getBody() instanceof ErrorResponse) {
                assertThat(((ErrorResponse) errorResponse.getBody()).getMessage(), equalTo(errorMessage));
            } else if (errorResponse.getBody() instanceof String) {
                assertThat(((String) errorResponse.getBody()).contains(errorMessage), is(true));
            } else {
                assertThat(((OAuthTokenResponse) errorResponse.getBody()).getErrorDescription(), equalTo(errorMessage));
            }
        });

        Then("^the introspected token is (active|expired)$", (String tokenStatus) -> {

            ResponseEntity<TokenIntrospectionResult> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().isActive(), equalTo("active".equals(tokenStatus)));
        });

        Then("^the introspected token expires in about (\\d+) seconds$", (Integer expirationInSeconds) -> {

            ResponseEntity<TokenIntrospectionResult> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            int currentExpirationInSeconds = (int) (response.getBody().getExpiration().getTime() - System.currentTimeMillis()) / 1000;
            assertThat(currentExpirationInSeconds <= expirationInSeconds && currentExpirationInSeconds > expirationInSeconds - 3, is(true));
        });

        Then("^the introspected token belongs to ([a-z0-9_]+) client$", (String clientID) -> {

            ResponseEntity<TokenIntrospectionResult> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getClientID().startsWith(clientID), is(true));
        });

        Then("^the introspected token belongs to the ([a-zA-Z_ ]+) user with ID (\\d+)$", (String username, Integer userID) -> {

            ResponseEntity<TokenIntrospectionResult> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(response.getBody().getUsername(), equalTo(username));
            assertThat(response.getBody().getClientID().endsWith(String.format("|uid=%d", userID)), is(true));
        });

        Then("^the userinfo response contains the key ([a-z]+) with a value of (.*)$", (String key, String value) -> {

            ResponseEntity<UserInfoResponse> response = ThreadLocalDataRegistry.getResponseEntity();
            assertThat(response.getBody(), notNullValue());
            assertThat(UserInfoResponse.FIELD_EXTRACTION_MAPPING.get(key).apply(response.getBody()), equalTo(value));
        });
    }
}
