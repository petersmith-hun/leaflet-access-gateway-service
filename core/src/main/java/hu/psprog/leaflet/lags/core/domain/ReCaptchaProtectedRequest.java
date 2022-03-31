package hu.psprog.leaflet.lags.core.domain;

/**
 * Interface for request domain classes providing possibility of ReCaptcha verification.
 *
 * @author Peter Smith
 */
public interface ReCaptchaProtectedRequest {

    /**
     * Returns the ReCaptcha token value stored in the relevant form.
     *
     * @return ReCaptcha token value
     */
    String getRecaptchaToken();
}
