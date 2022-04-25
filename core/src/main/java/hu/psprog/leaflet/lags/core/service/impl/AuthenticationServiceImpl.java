package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.ReCaptchaProtectedRequest;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.AuthenticationService;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.util.ReCaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of {@link AuthenticationService}.
 *
 * @author Peter Smith
 */
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ReCaptchaValidator reCaptchaValidator;
    private final AccountRequestHandler<SignUpRequestModel, SignUpResult> signUpRequestAccountRequestHandler;
    private final AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler;
    private final AccountRequestHandler<PasswordResetConfirmationRequestModel, Void> passwordResetConfirmationAccountRequestHandler;

    @Autowired
    public AuthenticationServiceImpl(ReCaptchaValidator reCaptchaValidator,
                                     AccountRequestHandler<SignUpRequestModel, SignUpResult> signUpRequestAccountRequestHandler,
                                     AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler,
                                     AccountRequestHandler<PasswordResetConfirmationRequestModel, Void> passwordResetConfirmationAccountRequestHandler) {

        this.reCaptchaValidator = reCaptchaValidator;
        this.signUpRequestAccountRequestHandler = signUpRequestAccountRequestHandler;
        this.passwordResetRequestAccountRequestHandler = passwordResetRequestAccountRequestHandler;
        this.passwordResetConfirmationAccountRequestHandler = passwordResetConfirmationAccountRequestHandler;
    }

    @Override
    public SignUpResult signUp(SignUpRequestModel signUpRequestModel, HttpServletRequest request) {

        return reCaptchaValidator.isValid(signUpRequestModel, request)
                ? signUpRequestAccountRequestHandler.processAccountRequest(signUpRequestModel)
                : SignUpResult.createByStatus(SignUpStatus.RE_CAPTCHA_VERIFICATION_FAILED);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestModel passwordResetRequestModel, HttpServletRequest request) {

        verifyReCaptcha(passwordResetRequestModel, request);
        passwordResetRequestAccountRequestHandler.processAccountRequest(passwordResetRequestModel);
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel, HttpServletRequest request) {

        verifyReCaptcha(passwordResetConfirmationRequestModel, request);
        passwordResetConfirmationAccountRequestHandler.processAccountRequest(passwordResetConfirmationRequestModel);
    }

    private void verifyReCaptcha(ReCaptchaProtectedRequest reCaptchaProtectedRequest, HttpServletRequest request) {

        if (!reCaptchaValidator.isValid(reCaptchaProtectedRequest, request)) {
            throw new AuthenticationException("Failed to verify ReCaptcha");
        }
    }
}
