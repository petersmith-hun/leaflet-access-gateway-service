package hu.psprog.leaflet.lags.core.service.notification.impl;

import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link LENSRequestAuthentication}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class LENSRequestAuthenticationTest {

    private static final GrantType GRANT_TYPE = GrantType.CLIENT_CREDENTIALS;
    private static final String CLIENT_ID = "lags-1";
    private static final String AUDIENCE = "audience";
    private static final List<String> SCOPE = List.of("write:mail:system_startup", "write:mail:signup_confirmation");
    private static final String ACCESS_TOKEN = "access-token-1";
    private static final int EXPIRES_IN = 600;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer access-token-1";
    private static final OAuthTokenRequest TOKEN_REQUEST = prepareTokenRequest();
    private static final OAuthTokenResponse TOKEN_RESPONSE = prepareTokenResponse(EXPIRES_IN);

    @Mock
    private OAuthAuthorizationService oAuthAuthorizationService;

    @InjectMocks
    private LENSRequestAuthentication lensRequestAuthentication;

    @BeforeEach
    public void setup() {
        lensRequestAuthentication.setClientID(CLIENT_ID);
        lensRequestAuthentication.setAudience(AUDIENCE);
        lensRequestAuthentication.setScope(SCOPE);
    }

    @Test
    public void shouldGetAuthenticationHeaderRequestTokenIfMissing() {

        // given
        given(oAuthAuthorizationService.authorize(TOKEN_REQUEST)).willReturn(TOKEN_RESPONSE);

        // when
        Map<String, String> result = lensRequestAuthentication.getAuthenticationHeader();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(AUTHORIZATION_HEADER), equalTo(BEARER_TOKEN));
        verify(oAuthAuthorizationService).authorize(TOKEN_REQUEST);
    }

    @Test
    public void shouldGetAuthenticationHeaderRequestTokenIfNearExpiration() {

        // given
        lensRequestAuthentication.setCurrentToken(prepareLENSToken(120));
        given(oAuthAuthorizationService.authorize(TOKEN_REQUEST)).willReturn(TOKEN_RESPONSE);

        // when
        Map<String, String> result = lensRequestAuthentication.getAuthenticationHeader();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(AUTHORIZATION_HEADER), equalTo(BEARER_TOKEN));
        verify(oAuthAuthorizationService).authorize(TOKEN_REQUEST);
    }

    @Test
    public void shouldGetAuthenticationHeaderRequestTokenIfExpired() {

        // given
        lensRequestAuthentication.setCurrentToken(prepareLENSToken(-30));
        given(oAuthAuthorizationService.authorize(TOKEN_REQUEST)).willReturn(TOKEN_RESPONSE);

        // when
        Map<String, String> result = lensRequestAuthentication.getAuthenticationHeader();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(AUTHORIZATION_HEADER), equalTo(BEARER_TOKEN));
        verify(oAuthAuthorizationService).authorize(TOKEN_REQUEST);
    }

    @Test
    public void shouldGetAuthenticationHeaderUseStoredToken() {

        // given
        lensRequestAuthentication.setCurrentToken(prepareLENSToken(360));

        // when
        Map<String, String> result = lensRequestAuthentication.getAuthenticationHeader();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(AUTHORIZATION_HEADER), equalTo(BEARER_TOKEN));
        verify(oAuthAuthorizationService, never()).authorize(TOKEN_REQUEST);
    }

    private static OAuthTokenRequest prepareTokenRequest() {

        return OAuthTokenRequest.builder()
                .grantType(GRANT_TYPE)
                .clientID(CLIENT_ID)
                .audience(AUDIENCE)
                .scope(SCOPE)
                .build();
    }

    private static OAuthTokenResponse prepareTokenResponse(int expiresIn) {

        return OAuthTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .expiresIn(expiresIn)
                .build();
    }

    private static LENSRequestAuthentication.LENSToken prepareLENSToken(int expiresIn) {
        return new LENSRequestAuthentication.LENSToken(prepareTokenResponse(expiresIn));
    }
}
