package hu.psprog.leaflet.lags.core.service.util;

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
}
