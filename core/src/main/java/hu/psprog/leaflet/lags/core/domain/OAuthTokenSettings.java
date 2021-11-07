package hu.psprog.leaflet.lags.core.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.nio.file.Path;

/**
 * OAuth2 JWT token configuration parameters model.
 *
 * @author Peter Smith
 */
@Data
@ConstructorBinding
public class OAuthTokenSettings {

    /**
     * Token expiration time in seconds.
     */
    private final int expiration;

    /**
     * Token issuer URI.
     */
    private final String issuer;

    /**
     * Path of the private RSA key file to sign the tokens with.
     */
    private final Path privateKeyFile;
}