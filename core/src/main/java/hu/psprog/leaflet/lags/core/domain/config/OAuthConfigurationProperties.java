package hu.psprog.leaflet.lags.core.domain.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * OAuth2 configuration properties model.
 *
 * @author Peter Smith
 */
@Data
@Setter(AccessLevel.PACKAGE)
@ConfigurationProperties(prefix = "oauth2-config")
public class OAuthConfigurationProperties {

    /**
     * OAuth2 JWT token configuration parameters.
     */
    private OAuthTokenSettings token;

    /**
     * Authorization code expiration in {@link Duration}.
     */
    private Duration authCodeExpiration;

    /**
     * OAuth2 client registrations.
     */
    private List<OAuthClient> clients = Collections.emptyList();
}
