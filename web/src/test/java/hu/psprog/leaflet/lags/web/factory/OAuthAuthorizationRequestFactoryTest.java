package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link OAuthAuthorizationRequestFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthAuthorizationRequestFactoryTest {

    private static final String CLIENT_ID = "client-1";
    private static final String REDIRECT_URI = "https://dev.local:9999/callback";
    private static final String STATE = "state-1";
    private static final String SCOPE = "scope1 scope2";

    @InjectMocks
    private OAuthAuthorizationRequestFactory oAuthAuthorizationRequestFactory;

    @Test
    public void shouldCreateAuthorizationRequestGenerateRequestObject() {

        // given
        Map<String, String> request = prepareRequestParameters();

        // when
        OAuthAuthorizationRequest result = oAuthAuthorizationRequestFactory.createAuthorizationRequest(request);

        // then
        assertThat(result, equalTo(OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.CODE)
                .clientID(CLIENT_ID)
                .redirectURI(REDIRECT_URI)
                .scope(SCOPE)
                .state(STATE)
                .build()));
    }

    @Test
    public void shouldCreateAuthorizationRequestThrowExceptionForInvalidResponseType() {

        // given
        Map<String, String> request = prepareRequestParameters(false, "none");

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationRequestFactory.createAuthorizationRequest(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Unsupported response type [invalid]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "client_id",
            "redirect_uri",
            "state"
    })
    public void shouldCreateAuthorizationRequestThrowExceptionForInvalidInput(String missingField) {

        // given
        Map<String, String> request = prepareRequestParameters(true, missingField);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationRequestFactory.createAuthorizationRequest(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("A mandatory field [%s] is missing from request", missingField)));
    }

    private static Map<String, String> prepareRequestParameters() {
        return prepareRequestParameters(true, "none");
    }

    private static Map<String, String> prepareRequestParameters(boolean withValidResponseType, String skipParameter) {

        return Map.of(
                "response_type", withValidResponseType ? "code" : "invalid",
                "client_id", skipParameter.equals("client_id") ? StringUtils.EMPTY : CLIENT_ID,
                "redirect_uri", skipParameter.equals("redirect_uri") ? StringUtils.EMPTY : REDIRECT_URI,
                "state", skipParameter.equals("state") ? StringUtils.EMPTY : STATE,
                "scope", SCOPE
        );
    }
}
