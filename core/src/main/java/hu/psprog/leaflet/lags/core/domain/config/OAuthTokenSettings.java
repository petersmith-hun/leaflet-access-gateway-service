package hu.psprog.leaflet.lags.core.domain.config;

import com.nimbusds.jose.JWSAlgorithm;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.nio.file.Path;

/**
 * OAuth2 JWT token configuration parameters model.
 *
 * @author Peter Smith
 */
@Data
@Setter(AccessLevel.PACKAGE)
public class OAuthTokenSettings {

    /**
     * Token expiration time in seconds.
     */
    private int expiration;

    /**
     * Token issuer URI.
     */
    private String issuer;

    /**
     * Path of the private RSA key file to sign the tokens with.
     */
    private Path privateKeyFile;

    /**
     * Path of the public RSA key file to sign the tokens with.
     */
    private Path publicKeyFile;

    /**
     * Key ID ("kid") header value for the generated JWT access tokens.
     */
    private String keyID;

    /**
     * JWT access token signature algorithm.
     */
    private JWSAlgorithm signatureAlgorithm;
}
