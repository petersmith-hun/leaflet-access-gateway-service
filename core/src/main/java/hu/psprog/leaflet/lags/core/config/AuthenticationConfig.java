package hu.psprog.leaflet.lags.core.config;

import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
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

    /**
     * Sets the enabled flag of the newly registered users to {@code true} if enabled.
     */
    private boolean userEnabledByDefault = true;

    /**
     * Default locale of the newly registered users.
     */
    private SupportedLocale defaultLocale = SupportedLocale.HU;

    /**
     * Google Recaptcha client secret.
     */
    private String recaptchaSecret;

    /**
     * Google Recaptcha site key.
     */
    private String recaptchaSiteKey;

    /**
     * Password reset process configuration parameters.
     */
    @NestedConfigurationProperty
    private PasswordResetConfig passwordReset;

    @Data
    public static class PasswordResetConfig {

        /**
         * Target OAuth audience for the issued password reset access tokens.
         */
        private String audience;

        /**
         * Password reset access token expiration in seconds.
         */
        private int tokenExpiration;

        /**
         * Client application return URL for the password reset process.
         */
        private String returnUrl;
    }
}
