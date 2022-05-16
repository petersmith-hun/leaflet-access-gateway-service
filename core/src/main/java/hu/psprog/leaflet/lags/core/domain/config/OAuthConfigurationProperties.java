package hu.psprog.leaflet.lags.core.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

/**
 * OAuth2 configuration properties model.
 *
 * @author Peter Smith
 */
@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "oauth2-config")
public class OAuthConfigurationProperties {

    /**
     * OAuth2 JWT token configuration parameters.
     */
    private final OAuthTokenSettings token;

    /**
     * OAuth2 client registrations.
     */
    private final List<OAuthClient> clients;
}
