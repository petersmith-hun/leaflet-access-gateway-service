package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link PasswordResetRequestAccountRequestHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetRequestAccountRequestHandlerTest {

    private static final long USER_ID = 1234L;
    private static final String USERNAME = "username1";
    private static final String EMAIL = "user@dev.local";
    private static final User USER = new User();

    private static final String AUDIENCE = "gateway-aud-1";
    private static final String ACCESS_TOKEN = "generated-token-1";
    private static final int EXPIRES_IN = 1800;

    private static final PasswordResetRequestModel PASSWORD_RESET_REQUEST_MODEL = new PasswordResetRequestModel();
    private static final OAuthTokenRequest EXPECTED_O_AUTH_TOKEN_REQUEST = OAuthTokenRequest.builder()
            .audience(AUDIENCE)
            .build();
    private static final OAuthTokenResponse O_AUTH_TOKEN_RESPONSE = OAuthTokenResponse.builder()
            .accessToken(ACCESS_TOKEN)
            .expiresIn(EXPIRES_IN)
            .build();
    private static final Map<String, Object> EXPECTED_CLAIMS = Map.of(
            OAuthConstants.Token.SCOPE, SecurityConstants.RECLAIM_AUTHORITY.getAuthority(),
            OAuthConstants.Token.SUBJECT, "password-reset|uid=1234",
            OAuthConstants.Token.ROLE, SecurityConstants.RECLAIM_ROLE,
            OAuthConstants.Token.USER, EMAIL,
            OAuthConstants.Token.NAME, USERNAME,
            OAuthConstants.Token.USER_ID, USER_ID
    );
    private static final PasswordResetRequest PASSWORD_RESET_REQUEST = PasswordResetRequest.builder()
            .username(USERNAME)
            .participant(EMAIL)
            .token(ACCESS_TOKEN)
            .expiration(EXPIRES_IN)
            .build();

    static {
        USER.setUsername(USERNAME);
        USER.setEmail(EMAIL);
        USER.setId(USER_ID);
        PASSWORD_RESET_REQUEST_MODEL.setEmail(EMAIL);
    }

    @Mock
    private UserDAO userDAO;

    @Mock
    private NotificationAdapter notificationAdapter;

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private AuthenticationConfig authenticationConfig;

    @Mock
    private AuthenticationConfig.PasswordResetConfig passwordResetConfig;

    private PasswordResetRequestAccountRequestHandler passwordResetRequestAccountRequestHandler;

    @BeforeEach
    public void setup() {

        given(authenticationConfig.getPasswordReset()).willReturn(passwordResetConfig);
        passwordResetRequestAccountRequestHandler = new PasswordResetRequestAccountRequestHandler(userDAO, notificationAdapter, tokenHandler, authenticationConfig);
    }

    @Test
    public void shouldProcessAccountRequestHandlePasswordResetRequestWithSuccess() {

        // given
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(USER));
        given(passwordResetConfig.getAudience()).willReturn(AUDIENCE);
        given(passwordResetConfig.getTokenExpiration()).willReturn(EXPIRES_IN);
        given(tokenHandler.generateToken(EXPECTED_O_AUTH_TOKEN_REQUEST, EXPECTED_CLAIMS, EXPIRES_IN)).willReturn(O_AUTH_TOKEN_RESPONSE);

        // when
        passwordResetRequestAccountRequestHandler.processAccountRequest(PASSWORD_RESET_REQUEST_MODEL);

        // then
        verify(tokenHandler).generateToken(EXPECTED_O_AUTH_TOKEN_REQUEST, EXPECTED_CLAIMS, EXPIRES_IN);
        verify(notificationAdapter).passwordResetRequested(PASSWORD_RESET_REQUEST);
    }

    @Test
    public void shouldProcessAccountRequestFailSilentlyIfUserAccountDoesNotExist() {

        // given
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.empty());

        // when
        passwordResetRequestAccountRequestHandler.processAccountRequest(PASSWORD_RESET_REQUEST_MODEL);

        // then
        verifyNoMoreInteractions(userDAO);
        verifyNoInteractions(passwordResetConfig, tokenHandler, notificationAdapter);
    }
}
