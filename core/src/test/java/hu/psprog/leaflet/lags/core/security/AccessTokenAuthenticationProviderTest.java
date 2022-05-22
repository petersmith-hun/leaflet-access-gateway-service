package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.exception.RevokedTokenException;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link AccessTokenAuthenticationProvider}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AccessTokenAuthenticationProviderTest {

    private static final String RAW_TOKEN = "token-1";
    private static final String TOKEN_ID = "token-id-1";
    private static final String AUDIENCE = "gateway-aud-1";
    private static final JWTAuthenticationToken JWT_AUTHENTICATION_TOKEN = JWTAuthenticationToken.getBuilder()
            .withRawToken(RAW_TOKEN)
            .withClaims(TokenClaims.builder()
                    .tokenID(TOKEN_ID)
                    .scopes(new String[] {"write:reclaim"})
                    .audience(AUDIENCE)
                    .build())
            .build();
    private static final JWTAuthenticationToken JWT_AUTHENTICATION_TOKEN_TOO_BROAD_AUDIENCE = JWTAuthenticationToken.getBuilder()
            .withRawToken(RAW_TOKEN)
            .withClaims(TokenClaims.builder()
                    .tokenID(TOKEN_ID)
                    .scopes(new String[] {"write:reclaim", "read:all", "write:all"})
                    .audience(AUDIENCE)
                    .build())
            .build();
    private static final JWTAuthenticationToken JWT_AUTHENTICATION_TOKEN_WITHOUT_RECLAIM_SCOPE = JWTAuthenticationToken.getBuilder()
            .withRawToken(RAW_TOKEN)
            .withClaims(TokenClaims.builder()
                    .tokenID(TOKEN_ID)
                    .scopes(new String[] {"read:all"})
                    .audience(AUDIENCE)
                    .build())
            .build();

    private static final StoreAccessTokenInfoRequest STORE_ACCESS_TOKEN_INFO_REQUEST = StoreAccessTokenInfoRequest.builder().id(TOKEN_ID).build();
    private static final AccessTokenInfo ACCESS_TOKEN_INFO = new AccessTokenInfo(STORE_ACCESS_TOKEN_INFO_REQUEST);

    @Mock
    private TokenTracker tokenTracker;

    @Mock
    private AuthenticationConfig authenticationConfig;

    @Mock
    private AuthenticationConfig.PasswordResetConfig passwordResetConfig;

    @InjectMocks
    private AccessTokenAuthenticationProvider accessTokenAuthenticationProvider;

    @Test
    public void shouldAuthenticateSuccessfullyAuthenticateTheGivenToken() {

        // given
        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_INFO));
        given(authenticationConfig.getPasswordReset()).willReturn(passwordResetConfig);
        given(passwordResetConfig.getAudience()).willReturn(AUDIENCE);

        // when
        Authentication result = accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN);

        // then
        assertThat(result.isAuthenticated(), is(true));
    }

    @Test
    public void shouldAuthenticateThrowExceptionForNonTrackedToken() {

        // given
        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.empty());

        // when
        assertThrows(RevokedTokenException.class, () -> accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN));

        // then
        // exception expected
    }

    @Test
    public void shouldAuthenticateThrowExceptionForInactiveToken() {

        // given
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo(STORE_ACCESS_TOKEN_INFO_REQUEST);
        accessTokenInfo.setStatus(TokenStatus.REVOKED);

        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.of(accessTokenInfo));

        // when
        assertThrows(RevokedTokenException.class, () -> accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN));

        // then
        // exception expected
    }

    @Test
    public void shouldAuthenticateThrowExceptionForTooBroadScope() {

        // given
        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_INFO));

        // when
        assertThrows(RevokedTokenException.class, () -> accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN_TOO_BROAD_AUDIENCE));

        // then
        // exception expected
    }

    @Test
    public void shouldAuthenticateThrowExceptionForNonReclaimScope() {

        // given
        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_INFO));

        // when
        assertThrows(RevokedTokenException.class, () -> accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN_WITHOUT_RECLAIM_SCOPE));

        // then
        // exception expected
    }

    @Test
    public void shouldAuthenticateThrowExceptionForUnacceptableAudience() {

        // given
        given(tokenTracker.retrieveTokenInfo(TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_INFO));
        given(authenticationConfig.getPasswordReset()).willReturn(passwordResetConfig);
        given(passwordResetConfig.getAudience()).willReturn("different-expected-audience");

        // when
        assertThrows(RevokedTokenException.class, () -> accessTokenAuthenticationProvider.authenticate(JWT_AUTHENTICATION_TOKEN));

        // then
        // exception expected
    }

    @ParameterizedTest
    @MethodSource("supportFlagDataProvider")
    public void shouldSupportOnlyJWTAuthenticationTokens(Class<?> authenticationClass, boolean expectedSupportFlag) {

        // when
        boolean result = accessTokenAuthenticationProvider.supports(authenticationClass);

        // then
        assertThat(result, is(expectedSupportFlag));
    }

    private static Stream<Arguments> supportFlagDataProvider() {

        return Stream.of(
                Arguments.of(JWTAuthenticationToken.class, true),
                Arguments.of(AbstractAuthenticationToken.class, false),
                Arguments.of(Authentication.class, false),
                Arguments.of(UsernamePasswordAuthenticationToken.class, false)
        );
    }
}
