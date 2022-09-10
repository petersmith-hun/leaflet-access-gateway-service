package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private static final String AUDIENCE = "gateway-aud-1";
    private static final String ACCESS_TOKEN = "generated-token-1";
    private static final int EXPIRES_IN = 1800;

    private static final User LOCAL_USER = prepareUser(AccountType.LOCAL);
    private static final User EXTERNAL_USER = prepareUser(AccountType.GITHUB);
    private static final PasswordResetRequestModel PASSWORD_RESET_REQUEST_MODEL = prepareResetRequestModel();
    private static final OAuthTokenRequest EXPECTED_O_AUTH_TOKEN_REQUEST = prepareTokenRequest();
    private static final OAuthTokenResponse O_AUTH_TOKEN_RESPONSE = prepareTokenResponse();
    private static final TokenClaims EXPECTED_CLAIMS = prepareClaims();
    private static final PasswordResetRequest PASSWORD_RESET_REQUEST = prepareResetRequest();

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
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(LOCAL_USER));
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
    public void shouldProcessAccountRequestFailSilentlyIfUserAccountIsExternal() {

        // given
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(EXTERNAL_USER));

        // when
        passwordResetRequestAccountRequestHandler.processAccountRequest(PASSWORD_RESET_REQUEST_MODEL);

        // then
        verifyNoMoreInteractions(userDAO);
        verifyNoInteractions(passwordResetConfig, tokenHandler, notificationAdapter);
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

    private static User prepareUser(AccountType accountType) {

        User user = new User();
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setId(USER_ID);
        user.setAccountType(accountType);
        
        return user;
    }

    private static PasswordResetRequestModel prepareResetRequestModel() {

        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();
        passwordResetRequestModel.setEmail(EMAIL);

        return passwordResetRequestModel;
    }

    private static OAuthTokenRequest prepareTokenRequest() {
        return OAuthTokenRequest.builder()
                .audience(AUDIENCE)
                .build();
    }

    private static OAuthTokenResponse prepareTokenResponse() {
        return OAuthTokenResponse.builder()
                .accessToken(ACCESS_TOKEN)
                .expiresIn(EXPIRES_IN)
                .build();
    }

    private static TokenClaims prepareClaims() {

        return TokenClaims.builder()
                .scope(SecurityConstants.RECLAIM_AUTHORITY.getAuthority())
                .subject("password-reset|uid=1234")
                .role(SecurityConstants.RECLAIM_ROLE)
                .email(EMAIL)
                .username(USERNAME)
                .userID(USER_ID)
                .build();
    }

    private static PasswordResetRequest prepareResetRequest() {
        return PasswordResetRequest.builder()
                .username(USERNAME)
                .participant(EMAIL)
                .token(ACCESS_TOKEN)
                .expiration(EXPIRES_IN)
                .build();
    }
}
