package hu.psprog.leaflet.lags.core.service.notification;

import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;

/**
 * System notification handler adapter.
 *
 * @author Peter Smith
 */
public interface NotificationAdapter {

    /**
     * Sends notification about successful sign-up.
     *
     * @param signUpConfirmation {@link SignUpConfirmation} domain object holding required parameters
     */
    void signUpConfirmation(SignUpConfirmation signUpConfirmation);

    /**
     * Sends response mail for password reset request.
     *
     * @param passwordResetRequest domain object holding required parameters
     */
    void passwordResetRequested(PasswordResetRequest passwordResetRequest);

    /**
     * Sends notification of successful password reset.
     *
     * @param passwordResetSuccess domain object holding required parameters
     */
    void successfulPasswordReset(PasswordResetSuccess passwordResetSuccess);
}
