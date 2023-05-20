package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.request.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.response.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.util.ReCaptchaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_SIGNUP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link AuthenticationServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final SignUpRequestModel SIGN_UP_REQUEST_MODEL = new SignUpRequestModel();
    private static final SignUpResult SIGN_UP_RESULT = new SignUpResult(PATH_LOGIN, SignUpStatus.SUCCESS);
    private static final PasswordResetRequestModel PASSWORD_RESET_REQUEST_MODEL = new PasswordResetRequestModel();
    private static final PasswordResetConfirmationRequestModel PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL = new PasswordResetConfirmationRequestModel();

    @Mock
    private ReCaptchaValidator reCaptchaValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AccountRequestHandler<SignUpRequestModel, SignUpResult> signUpRequestAccountRequestHandler;

    @Mock
    private AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler;

    @Mock
    private AccountRequestHandler<PasswordResetConfirmationRequestModel, Void> passwordResetConfirmationAccountRequestHandler;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    public void setup() {
        authenticationService = new AuthenticationServiceImpl(reCaptchaValidator, signUpRequestAccountRequestHandler,
                passwordResetRequestAccountRequestHandler, passwordResetConfirmationAccountRequestHandler);
    }

    @Test
    public void shouldHandleSignUpWithSuccess() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(true);
        given(signUpRequestAccountRequestHandler.processAccountRequest(SIGN_UP_REQUEST_MODEL)).willReturn(SIGN_UP_RESULT);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.redirectURI(), equalTo(PATH_LOGIN));
        assertThat(result.signUpStatus(), equalTo(SignUpStatus.SUCCESS));
    }

    @Test
    public void shouldSignUpReturnReCaptchaVerificationFailure() {

        // given
        given(reCaptchaValidator.isValid(SIGN_UP_REQUEST_MODEL, request)).willReturn(false);

        // when
        SignUpResult result = authenticationService.signUp(SIGN_UP_REQUEST_MODEL, request);

        // then
        assertThat(result.redirectURI(), equalTo(PATH_SIGNUP));
        assertThat(result.signUpStatus(), equalTo(SignUpStatus.RE_CAPTCHA_VERIFICATION_FAILED));

        verifyNoInteractions(signUpRequestAccountRequestHandler);
    }

    @Test
    public void shouldRequestPasswordResetProcessTheRequestWithSuccess() {

        // given
        given(reCaptchaValidator.isValid(PASSWORD_RESET_REQUEST_MODEL, request)).willReturn(true);

        // when
        authenticationService.requestPasswordReset(PASSWORD_RESET_REQUEST_MODEL, request);

        // verify
        verify(passwordResetRequestAccountRequestHandler).processAccountRequest(PASSWORD_RESET_REQUEST_MODEL);
    }

    @Test
    public void shouldRequestPasswordResetThrowExceptionOnFailedReCaptchaVerification() {

        // given
        given(reCaptchaValidator.isValid(PASSWORD_RESET_REQUEST_MODEL, request)).willReturn(false);

        // when
        assertThrows(AuthenticationException.class, () -> authenticationService.requestPasswordReset(PASSWORD_RESET_REQUEST_MODEL, request));

        // verify
        // exception expected
    }

    @Test
    public void shouldConfirmPasswordResetProcessTheRequestWithSuccess() {

        // given
        given(reCaptchaValidator.isValid(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL, request)).willReturn(true);

        // when
        authenticationService.confirmPasswordReset(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL, request);

        // verify
        verify(passwordResetConfirmationAccountRequestHandler).processAccountRequest(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL);
    }

    @Test
    public void shouldConfirmPasswordResetThrowExceptionOnFailedReCaptchaVerification() {

        // given
        given(reCaptchaValidator.isValid(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL, request)).willReturn(false);

        // when
        assertThrows(AuthenticationException.class, () -> authenticationService.confirmPasswordReset(PASSWORD_RESET_CONFIRMATION_REQUEST_MODEL, request));

        // verify
        // exception expected
    }
}
