package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.notification.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import hu.psprog.leaflet.lags.core.service.token.impl.JWTTokenHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link PasswordResetConfirmationAccountRequestHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetConfirmationAccountRequestHandlerTest {

    private static final String USERNAME = "username1";
    private static final String EMAIL = "user@dev.local";
    private static final String PASSWORD_NEW = "password-new";
    private static final String TOKEN_ID = "token-id-1";
    private static final String PASSWORD_NEW_ENCODED = "password-new-encoded";
    private static final PasswordResetConfirmationRequestModel PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL = new PasswordResetConfirmationRequestModel();
    private static final TokenClaims TOKEN_CLAIMS = TokenClaims.builder()
            .tokenID(TOKEN_ID)
            .email(EMAIL)
            .build();
    private static final User USER = new User();

    static {
        PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL.setPassword(PASSWORD_NEW);
        USER.setUsername(USERNAME);
        USER.setEmail(EMAIL);
        USER.setPassword("password-old");
    }

    @Mock
    private UserDAO userDAO;

    @Mock
    private NotificationAdapter notificationAdapter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenTracker tokenTracker;

    @Mock
    private JwtAuthenticationToken authentication;

    @Mock
    private JWTTokenHandler jwtTokenHandler;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private PasswordResetConfirmationAccountRequestHandler passwordResetConfirmationAccountRequestHandler;

    @Test
    public void shouldProcessAccountRequestHandlePasswordResetConfirmationWithSuccess() {

        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(authentication.getToken()).willReturn(jwt);
        given(jwtTokenHandler.extractClaims(jwt)).willReturn(TOKEN_CLAIMS);
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(USER));
        given(passwordEncoder.encode(PASSWORD_NEW)).willReturn(PASSWORD_NEW_ENCODED);

        // when
        passwordResetConfirmationAccountRequestHandler.processAccountRequest(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL);

        // then
        assertThat(USER.getPassword(), equalTo(PASSWORD_NEW_ENCODED));
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());

        verify(userDAO).save(USER);
        verify(tokenTracker).revokeToken(TOKEN_ID);
        verify(notificationAdapter).successfulPasswordReset(PasswordResetSuccess.builder()
                .username(USERNAME)
                .recipient(EMAIL)
                .build());
    }

    @Test
    public void shouldProcessAccountRequestRevokeTokenIfRelevantAccountDoesNotExist() {

        // given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        given(authentication.getToken()).willReturn(jwt);
        given(jwtTokenHandler.extractClaims(jwt)).willReturn(TOKEN_CLAIMS);
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.empty());

        // when
        passwordResetConfirmationAccountRequestHandler.processAccountRequest(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication(), nullValue());

        verify(tokenTracker).revokeToken(TOKEN_ID);
        verifyNoMoreInteractions(userDAO);
        verifyNoInteractions(passwordEncoder, notificationAdapter);
    }
}
