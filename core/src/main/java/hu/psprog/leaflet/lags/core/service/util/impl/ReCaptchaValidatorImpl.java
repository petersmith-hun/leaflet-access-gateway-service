package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.ReCaptchaProtectedRequest;
import hu.psprog.leaflet.lags.core.service.util.ReCaptchaValidator;
import hu.psprog.leaflet.recaptcha.api.client.ReCaptchaClient;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaRequest;
import hu.psprog.leaflet.recaptcha.api.domain.ReCaptchaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of {@link ReCaptchaValidator}.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class ReCaptchaValidatorImpl implements ReCaptchaValidator {

    private final ReCaptchaClient reCaptchaClient;
    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public ReCaptchaValidatorImpl(ReCaptchaClient reCaptchaClient, AuthenticationConfig authenticationConfig) {
        this.reCaptchaClient = reCaptchaClient;
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    public boolean isValid(ReCaptchaProtectedRequest reCaptchaResponse, HttpServletRequest request) {

        log.info("Performing ReCaptcha validation...");

        ReCaptchaRequest reCaptchaRequest = ReCaptchaRequest.getBuilder()
                .withResponse(reCaptchaResponse.getRecaptchaToken())
                .withSecret(authenticationConfig.getRecaptchaSecret())
                .withRemoteIp(request.getRemoteAddr())
                .build();

        boolean successful = false;
        try {
            ReCaptchaResponse recaptchaVerificationResponse = reCaptchaClient.validate(reCaptchaRequest);
            successful = recaptchaVerificationResponse.isSuccessful();

            if (!successful) {
                log.error("Failed to verify ReCaptcha token - verification service response: {}", recaptchaVerificationResponse.getErrorCodes());
            }
        } catch (CommunicationFailureException e) {
            log.error("Failed to contact ReCaptcha verification service.", e);
        }

        return successful;
    }
}
