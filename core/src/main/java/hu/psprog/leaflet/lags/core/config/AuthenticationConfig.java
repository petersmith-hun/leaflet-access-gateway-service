package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Authentication configuration parameters model.
 * Expects the {@code authentication-config} parameter to be present in the application config.
 * Under that, the following parameters are expected:
 *  - user-enabled-by-default: marks the user account enabled right after registered if set to true (defaults to true)
 *  - default-locale: sets the default user account locale (defaults to Hungarian)
 *  - recaptcha-secret: Google ReCaptcha account secret (private)
 *  - recaptcha-key: Google ReCaptcha site key (public, exposed for JavaScript front-end code)
 *
 * @author Peter Smith
 */
@Data
@Component
@ConfigurationProperties(prefix = "authentication-config")
public class AuthenticationConfig {

    private boolean userEnabledByDefault = true;
    private SupportedLocale defaultLocale = SupportedLocale.HU;
    private String recaptchaSecret;
    private String recaptchaSiteKey;
    private PasswordResetConfig passwordReset;

    @Data
    public static class PasswordResetConfig {

        private String audience;
        private int tokenExpiration;
        private String returnUrl;
    }
}
