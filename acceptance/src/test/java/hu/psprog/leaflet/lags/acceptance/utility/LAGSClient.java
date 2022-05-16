package hu.psprog.leaflet.lags.acceptance.utility;

import hu.psprog.leaflet.lags.acceptance.model.ApplicationInfoResponse;
import hu.psprog.leaflet.lags.acceptance.model.ErrorResponse;
import hu.psprog.leaflet.lags.acceptance.model.HealthCheckResponse;
import hu.psprog.leaflet.lags.acceptance.model.OAuthTokenResponse;
import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import hu.psprog.leaflet.lags.acceptance.model.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;

import java.net.URI;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_SIGNUP;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_AUTHORIZE;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_INTROSPECT;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_TOKEN;
import static junit.framework.TestCase.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * HTTP client for the Leaflet Access Gateway Service.
 * This client is specifically implemented for acceptance test purposes, using the thread-local data registry
 * for collecting the call parameters.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class LAGSClient {

    private static final String PATH_HEALTH_CHECK = "/actuator/health";
    private static final String PATH_APPLICATION_INFO = "/actuator/info";
    private static final String PATH_LOGOUT = "/logout";

    private final LAGSRequestHelper lagsRequestHelper;
    private final TestRestTemplate restTemplate;
    private final MockMvc mockMvc;

    private final String tokenEndpoint;
    private final String authorizationEndpoint;
    private final String tokenIntrospectionEndpoint;
    private final String healthCheckEndpoint;
    private final String applicationInfoEndpoint;

    @Autowired
    public LAGSClient(LAGSRequestHelper lagsRequestHelper, TestRestTemplate testRestTemplate, MockMvc mockMvc, String baseServerPath) {
        this.lagsRequestHelper = lagsRequestHelper;
        this.restTemplate = testRestTemplate;
        this.mockMvc = mockMvc;
        this.tokenEndpoint = baseServerPath + PATH_OAUTH_TOKEN;
        this.authorizationEndpoint = baseServerPath + PATH_OAUTH_AUTHORIZE;
        this.tokenIntrospectionEndpoint = baseServerPath + PATH_OAUTH_INTROSPECT;
        this.healthCheckEndpoint = baseServerPath + PATH_HEALTH_CHECK;
        this.applicationInfoEndpoint = baseServerPath + PATH_APPLICATION_INFO;
    }

    /**
     * Requests an OAuth access token.
     *
     * Prepares the OAuth client authorization header and a form containing the necessary parameters from the
     * TOKEN_REQUEST_FORM_ATTRIBUTES list, then sends a POST request to the /oauth/token endpoint.
     *
     * @return response for the token request as {@link OAuthTokenResponse} wrapped in {@link ResponseEntity}
     */
    public ResponseEntity<OAuthTokenResponse> requestToken() {

        log.info("Calling /oauth/token endpoint...");

        HttpHeaders headers = AuthorizationUtility.generateOAuthBasicAuthorization()
                .map(lagsRequestHelper::prepareAuthenticatedFormHeader)
                .orElseGet(HttpHeaders::new);
        MultiValueMap<String, String> formData = lagsRequestHelper.prepareTokenRequestForm();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        return restTemplate.postForEntity(tokenEndpoint, request, OAuthTokenResponse.class);
    }

    /**
     * Starts an OAuth authorization process.
     *
     * Prepares a user basic authorization header, and a query string containing the necessary parameters from the
     * AUTH_CODE_QUERY_PARAMETERS list, then sends a POST request to the /oauth/authorization endpoint.
     *
     * @return location header value as {@link URI} object
     */
    public ResponseEntity<ErrorResponse> requestAuthorization() {

        log.info("Calling /oauth/authorization endpoint...");

        String url = String.format("%s?%s", authorizationEndpoint, lagsRequestHelper.prepareAuthCodeQueryString());
        HttpHeaders headers = lagsRequestHelper.prepareAuthenticatedFormHeader(ThreadLocalDataRegistry.get(TestConstants.Attribute.USER_AUTH));
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.postForEntity(url, request, ErrorResponse.class);
    }

    /**
     * Requests a token introspection.
     *
     * Prepares an introspection form and sends a POST request to the /oauth/introspect endpoint.
     *
     * @return response for the token introspection request as {@link TokenIntrospectionResult} wrapped in {@link ResponseEntity}
     */
    public ResponseEntity<TokenIntrospectionResult> requestIntrospection() {

        log.info("Calling /oauth/introspect endpoint...");

        HttpHeaders headers = AuthorizationUtility.generateOAuthBasicAuthorization()
                .map(lagsRequestHelper::prepareAuthenticatedFormHeader)
                .orElseGet(HttpHeaders::new);
        MultiValueMap<String, String> formData = lagsRequestHelper.prepareTokenIntrospectionForm();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        return restTemplate.postForEntity(tokenIntrospectionEndpoint, request, TokenIntrospectionResult.class);
    }

    /**
     * Requests a form-based login by calling the /login endpoint.
     *
     * @return response without response body as {@link ResponseEntity}
     */
    public ResponseEntity<String> requestLogin() {

        log.info("Calling /login endpoint...");

        MvcResult result = null;
        try {
            result = mockMvc.perform(formLogin()
                            .user("email", ThreadLocalDataRegistry.get(TestConstants.Attribute.EMAIL))
                            .password(ThreadLocalDataRegistry.get(TestConstants.Attribute.PASSWORD)))
                    .andReturn();
        } catch (Exception exception) {
            fail(String.format("Login attempt failed: %s", exception.getMessage()));
        }

        return convertToResponseEntity(result);
    }

    /**
     * Requests a form-based sign-up.
     *
     * Prepares a sign-up form containing the necessary parameters defined in the SIGN_UP_FORM_ATTRIBUTES attribute list,
     * then sends the request to the /signup endpoint.
     *
     * @return response without response body as {@link ResponseEntity}
     */
    public ResponseEntity<String> requestSignUp() {

        log.info("Calling /signup endpoint...");

        MultiValueMap<String, String> formData = lagsRequestHelper.prepareSignUpRequestForm();

        MvcResult result = null;
        try {
            result = mockMvc.perform(post(PATH_SIGNUP)
                            .params(formData)
                            .with(csrf()))
                    .andReturn();
        } catch (Exception exception) {
            fail(String.format("Login attempt failed: %s", exception.getMessage()));
        }

        return convertToResponseEntity(result);
    }

    /**
     * Requests a form-based sign-out.
     *
     * Prepares a user basic authorization header and sends the request to the /logout endpoint.
     *
     * @return response without response body as {@link ResponseEntity}
     */
    public ResponseEntity<String> requestSignOut() {

        log.info("Calling /logout endpoint...");

        HttpHeaders headers = lagsRequestHelper.prepareAuthenticatedFormHeader(ThreadLocalDataRegistry.get(TestConstants.Attribute.USER_AUTH));

        MvcResult result = null;
        try {
            result = mockMvc.perform(post(PATH_LOGOUT)
                            .headers(headers)
                            .with(csrf()))
                    .andReturn();
        } catch (Exception exception) {
            fail(String.format("Logout attempt failed: %s", exception.getMessage()));
        }

        return convertToResponseEntity(result);
    }

    /**
     * Requests the start of a password reset process.
     *
     * Prepares a reset request form based on the necessary parameters defined in the PASSWORD_RESET_REQUEST_FORM_ATTRIBUTES
     * attribute list, then sends the request to the /password-reset endpoint.
     *
     * @return response with the response body as string wrapped as {@link ResponseEntity}
     */
    public ResponseEntity<String> requestPasswordReset() {

        log.info("Calling /password-reset endpoint...");

        MultiValueMap<String, String> formData = lagsRequestHelper.preparePasswordResetRequestForm();

        MvcResult result = null;
        try {
            result = mockMvc.perform(post(PATH_PASSWORD_RESET)
                            .params(formData)
                            .with(csrf()))
                    .andReturn();
        } catch (Exception exception) {
            fail(String.format("Password reset request attempt failed: %s", exception.getMessage()));
        }

        return convertToResponseEntity(result);
    }

    /**
     * Requests confirmation (password update) of a password reset process.
     *
     * Prepares a reset confirmation form based on the necessary parameters defined in the PASSWORD_RESET_CONFIRMATION_FORM_ATTRIBUTES
     * attribute list, also appends the reset token to the reset confirmation URL, then sends the request to the /password-reset/confirmation endpoint.
     *
     * @return response without response body as {@link ResponseEntity}
     */
    public ResponseEntity<String> requestPasswordResetConfirmation() {

        log.info("Calling /password-reset/confirmation endpoint...");

        MultiValueMap<String, String> formData = lagsRequestHelper.preparePasswordResetConfirmationForm();
        String url = String.format("%s?%s", PATH_PASSWORD_RESET_CONFIRMATION, lagsRequestHelper.preparePasswordResetConfirmationQueryString());

        MvcResult result = null;
        try {
            result = mockMvc.perform(post(url)
                            .params(formData)
                            .with(csrf()))
                    .andReturn();

        } catch (AuthenticationException exception) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(exception.getMessage());

        } catch (Exception exception) {
            fail(String.format("Password reset confirmation request attempt failed: %s", exception.getMessage()));
        }

        return convertToResponseEntity(result);
    }

    /**
     * Requests health check by sending a GET request to the /actuator/health endpoint.
     *
     * @return health check response as {@link HealthCheckResponse} wrapped in {@link ResponseEntity}
     */
    public ResponseEntity<HealthCheckResponse> requestHealthCheck() {

        log.info("Calling /actuator/health endpoint...");

        return this.restTemplate.getForEntity(healthCheckEndpoint, HealthCheckResponse.class);
    }

    /**
     * Requests application info by sending a GET request to the /actuator/info endpoint.
     *
     * @return application info response as {@link ApplicationInfoResponse} wrapped in {@link ResponseEntity}
     */
    public ResponseEntity<ApplicationInfoResponse> requestApplicationInfo() {

        log.info("Calling /actuator/info endpoint...");

        return this.restTemplate.getForEntity(applicationInfoEndpoint, ApplicationInfoResponse.class);
    }

    private ResponseEntity<String> convertToResponseEntity(MvcResult mvcResult) {

        MockHttpServletResponse response = mvcResult.getResponse();
        String locationHeader = TestConstants.Header.LOCATION.getValue();

        return ResponseEntity
                .status(response.getStatus())
                .header(locationHeader, response.getHeader(locationHeader))
                .body(new String(response.getContentAsByteArray()));
    }
}
