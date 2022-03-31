package hu.psprog.leaflet.lags.core.service.util;

import hu.psprog.leaflet.lags.core.domain.ReCaptchaProtectedRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Validates a captcha-protected request with Google ReCaptcha service.
 *
 * @author Peter Smith
 */
public interface ReCaptchaValidator {

    /**
     * Validates given {@link ReCaptchaProtectedRequest} by calling the Google ReCaptcha Service with the ReCaptcha
     * token provided by the widget on the ReCaptcha protected form.
     *
     * @param reCaptchaProtectedRequest {@link ReCaptchaProtectedRequest} object to validate
     * @return {@code true} if given request is valid, {@code false otherwise}
     */
    boolean isValid(ReCaptchaProtectedRequest reCaptchaProtectedRequest, HttpServletRequest request);
}
