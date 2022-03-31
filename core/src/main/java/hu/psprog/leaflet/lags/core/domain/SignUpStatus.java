package hu.psprog.leaflet.lags.core.domain;

/**
 * Possible sign-up request results.
 *
 * @author Peter Smith
 */
public enum SignUpStatus {

    /**
     * Sign-up request has been successfully processed.
     */
    SUCCESS,

    /**
     * Given email address is already in use.
     */
    ADDRESS_IN_USE,

    /**
     * ReCaptcha verification failed, missing, invalid or expired token was provided.
     */
    RE_CAPTCHA_VERIFICATION_FAILED,

    /**
     * Other processing error.
     */
    FAILURE
}
