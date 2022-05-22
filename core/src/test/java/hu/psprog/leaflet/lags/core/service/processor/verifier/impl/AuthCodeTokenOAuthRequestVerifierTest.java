package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AuthCodeTokenOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthCodeTokenOAuthRequestVerifierTest {

    @InjectMocks
    private AuthCodeTokenOAuthRequestVerifier authCodeTokenOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthTokenRequestContext context = prepareContext("auth-code-1", "http://localhost:9999/callback");

        // when
        authCodeTokenOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', nullValues = "null", value = {
            "'';redirect;code",
            "null;redirect;code",
            "code;'';redirect_uri",
            "code;null;redirect_uri"
    })
    public void shouldVerifyRejectContextForMissingField(String authCode, String redirectURI, String expectedMissingFieldName) {

        // given
        OAuthTokenRequestContext context = prepareContext(authCode, redirectURI);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authCodeTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", expectedMissingFieldName)));
    }

    @Test
    public void shouldForGrantTypeReturnAuthCode() {

        // when
        List<GrantType> result = authCodeTokenOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(GrantType.AUTHORIZATION_CODE));
    }

    private OAuthTokenRequestContext prepareContext(String authCode, String redirectURI) {

        return OAuthTokenRequestContext.builder()
                .request(OAuthTokenRequest.builder()
                        .authorizationCode(authCode)
                        .redirectURI(redirectURI)
                        .build())
                .build();
    }
}
