package hu.psprog.leaflet.lags.acceptance.utility;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.QUERY_PARAMETER_TOKEN;

/**
 * Utilities for creating specific requests for LAGS.
 *
 * @author Peter Smith
 */
@Component
public class LAGSRequestHelper {

    private static final List<TestConstants.Attribute> TOKEN_REQUEST_FORM_ATTRIBUTES = prepareTokenRequestFormAttributeList();
    private static final List<TestConstants.Attribute> AUTH_CODE_QUERY_PARAMETERS = prepareAuthCodeQueryParameterList();
    private static final List<TestConstants.Attribute> SIGN_UP_FORM_ATTRIBUTES = prepareSignUpFormAttributeList();
    private static final List<TestConstants.Attribute> PASSWORD_RESET_REQUEST_FORM_ATTRIBUTES = preparePasswordResetRequestFormAttributeList();
    private static final List<TestConstants.Attribute> PASSWORD_RESET_CONFIRMATION_FORM_ATTRIBUTES = preparePasswordResetConfirmationFormAttributeList();
    private static final List<TestConstants.Attribute> TOKEN_INTROSPECTION_FORM_ATTRIBUTES = prepareTokenIntrospectionAttributeList();
    private static final Map<TestConstants.Attribute, TestConstants.Flag> FLAGGED_ATTRIBUTE_MAP = prepareFlaggedAttributeMap();

    /**
     * Creates an Authorization header for forms requiring an authenticated entity (user or service).
     *
     * @param authorizationHeaderValue the authorization header value to be
     * @return prepared {@link HttpHeaders} object
     */
    public HttpHeaders prepareAuthenticatedFormHeader(String authorizationHeaderValue) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add(TestConstants.Header.AUTHORIZATION.getValue(), authorizationHeaderValue);

        return headers;
    }

    /**
     * Creates a form for OAuth2 token request.
     *
     * @return filled form as {@link MultiValueMap}
     */
    public MultiValueMap<String, String> prepareTokenRequestForm() {
        return prepareRequestForm(TOKEN_REQUEST_FORM_ATTRIBUTES);
    }

    /**
     * Creates a form for sign-up request.
     *
     * @return filled form as {@link MultiValueMap}
     */
    public MultiValueMap<String, String> prepareSignUpRequestForm() {
        return prepareRequestForm(SIGN_UP_FORM_ATTRIBUTES);
    }

    /**
     * Creates a form for password reset request.
     *
     * @return filled form as {@link MultiValueMap}
     */
    public MultiValueMap<String, String> preparePasswordResetRequestForm() {
        return prepareRequestForm(PASSWORD_RESET_REQUEST_FORM_ATTRIBUTES);
    }

    /**
     * Creates a form for password reset confirmation.
     *
     * @return filled form as {@link MultiValueMap}
     */
    public MultiValueMap<String, String> preparePasswordResetConfirmationForm() {
        return prepareRequestForm(PASSWORD_RESET_CONFIRMATION_FORM_ATTRIBUTES);
    }

    /**
     * Creates a form for token introspection.
     *
     * @return filled form as {@link MultiValueMap}
     */
    public MultiValueMap<String, String> prepareTokenIntrospectionForm() {
        return prepareRequestForm(TOKEN_INTROSPECTION_FORM_ATTRIBUTES);
    }

    /**
     * Creates a query parameter string for starting an authorization code flow based authorization process.
     *
     * @return created query string
     */
    public String prepareAuthCodeQueryString() {

        return AUTH_CODE_QUERY_PARAMETERS.stream()
                .filter(parameter -> Objects.nonNull(ThreadLocalDataRegistry.get(parameter)))
                .map(parameter -> String.format("%s=%s", parameter.getValue(), ThreadLocalDataRegistry.get(parameter)))
                .collect(Collectors.joining("&"));
    }

    /**
     * Creates a query parameter string for password reset confirmation request.
     *
     * @return created query string
     */
    public String preparePasswordResetConfirmationQueryString() {
        return String.format("%s=%s", QUERY_PARAMETER_TOKEN, ThreadLocalDataRegistry.get(TestConstants.Attribute.USER_AUTH));
    }

    private MultiValueMap<String, String> prepareRequestForm(List<TestConstants.Attribute> attributeList) {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        attributeList.stream()
                .filter(attribute -> Optional.ofNullable(FLAGGED_ATTRIBUTE_MAP.get(attribute))
                        .map(ThreadLocalDataRegistry::getFlag)
                        .orElse(true))
                .filter(attribute -> Objects.nonNull(ThreadLocalDataRegistry.get(attribute)))
                .forEach(attribute -> formData.add(attribute.getValue(), ThreadLocalDataRegistry.get(attribute)));

        return formData;
    }

    private static List<TestConstants.Attribute> prepareTokenRequestFormAttributeList() {

        return List.of(
                TestConstants.Attribute.AUDIENCE,
                TestConstants.Attribute.CLIENT_ID,
                TestConstants.Attribute.CODE,
                TestConstants.Attribute.GRANT_TYPE,
                TestConstants.Attribute.REDIRECT_URI,
                TestConstants.Attribute.SCOPE
        );
    }

    private static List<TestConstants.Attribute> prepareAuthCodeQueryParameterList() {

        return List.of(
                TestConstants.Attribute.CLIENT_ID,
                TestConstants.Attribute.REDIRECT_URI,
                TestConstants.Attribute.RESPONSE_TYPE,
                TestConstants.Attribute.STATE,
                TestConstants.Attribute.SCOPE
        );
    }

    private static List<TestConstants.Attribute> prepareSignUpFormAttributeList() {

        return List.of(
                TestConstants.Attribute.EMAIL,
                TestConstants.Attribute.PASSWORD,
                TestConstants.Attribute.PASSWORD_CONFIRM,
                TestConstants.Attribute.RECAPTCHA_TOKEN,
                TestConstants.Attribute.USERNAME
        );
    }

    private static List<TestConstants.Attribute> preparePasswordResetRequestFormAttributeList() {

        return List.of(
                TestConstants.Attribute.EMAIL,
                TestConstants.Attribute.RECAPTCHA_TOKEN
        );
    }

    private static List<TestConstants.Attribute> preparePasswordResetConfirmationFormAttributeList() {

        return List.of(
                TestConstants.Attribute.PASSWORD,
                TestConstants.Attribute.PASSWORD_CONFIRM,
                TestConstants.Attribute.RECAPTCHA_TOKEN
        );
    }

    private static List<TestConstants.Attribute> prepareTokenIntrospectionAttributeList() {
        return List.of(TestConstants.Attribute.TOKEN);
    }

    private static Map<TestConstants.Attribute, TestConstants.Flag> prepareFlaggedAttributeMap() {

        return Map.of(
                TestConstants.Attribute.CODE, TestConstants.Flag.USE_AUTHORIZATION_CODE,
                TestConstants.Attribute.RECAPTCHA_TOKEN, TestConstants.Flag.USE_RECAPTCHA_VERIFICATION,
                TestConstants.Attribute.REDIRECT_URI, TestConstants.Flag.USE_REDIRECT_URI
        );
    }
}
