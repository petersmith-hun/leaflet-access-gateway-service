package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.response.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Service layer for authentication related operations.
 *
 * @author Peter Smith
 */
public interface AuthenticationService {

    /**
     * Processes a sign-up request.
     *
     * @param signUpRequestModel {@link SignUpRequestModel} object containing data of the user to be registered
     * @param request {@link HttpServletRequest} object to gather additional request information (like redirection URI)
     * @return result of sign-up request processing as {@link SignUpStatus}
     */
    SignUpResult signUp(SignUpRequestModel signUpRequestModel, HttpServletRequest request);

    /**
     * Processes a password reset request.
     *
     * @param passwordResetRequestModel {@link PasswordResetRequestModel} model containing the email address of the account to request password reset for
     * @param request {@link HttpServletRequest} object to extract ReCaptcha token from
     */
    void requestPasswordReset(PasswordResetRequestModel passwordResetRequestModel, HttpServletRequest request);

    /**
     * Processes a password reset confirmation request.
     * Identification of the user account to be updated happens based on the given access token.
     *
     * @param passwordResetConfirmationRequestModel {@link PasswordResetConfirmationRequestModel} model containing the new password of the
     * @param request {@link HttpServletRequest} object to extract ReCaptcha and access tokens from
     */
    void confirmPasswordReset(PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel, HttpServletRequest request);
}
