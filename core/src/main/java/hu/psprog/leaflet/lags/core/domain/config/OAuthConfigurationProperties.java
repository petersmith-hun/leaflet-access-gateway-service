package hu.psprog.leaflet.lags.core.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * OAuth2 configuration properties model.
 *
 * @author Peter Smith
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth2-config")
public class OAuthConfigurationProperties {

    /**
     * Enables automatically importing legacy application registrations.
     */
    private boolean autoImport;

    /**
     * OAuth2 JWT token configuration parameters.
     */
    @NestedConfigurationProperty
    private OAuthTokenSettings token;

    /**
     * Authorization code expiration in {@link Duration}.
     */
    private Duration authCodeExpiration;

    /**
     * Default redirect URI on authorization failure (in case the redirect URI is not specified or invalid).
     */
    private String defaultRedirectOnError;

    /**
     * Enables legacy OAuth application registration mode (using static configuration from a Spring Boot managed configuration file).
     */
    private boolean enableLegacyRegistration;

    /**
     * OAuth2 client registrations.
     */
    @NestedConfigurationProperty
    private List<OAuthClient> clients = Collections.emptyList();
}
