package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.service.util.KeyRegistry;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link KeyRegistry} implementation for RSA keys.
 * Can be used for signing JWT tokens.
 *
 * Upon instantiation this implementation loads the private RSA key defined by
 * {@code oauth2-config.token.private-key-file} configuration parameter.
 *
 * @author Peter Smith
 */
@Component
public class RSAKeyRegistry implements KeyRegistry {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    private PrivateKey rsaPrivateKey;

    @Autowired
    public RSAKeyRegistry(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    @PostConstruct
    public void readRSAKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {

        List<String> rsaKeyLines = Files.readAllLines(oAuthConfigurationProperties.getToken().getPrivateKeyFile());
        String rsaKeyContent = rsaKeyLines.stream()
                .limit(rsaKeyLines.size() - 1)
                .skip(1)
                .collect(Collectors.joining());
        byte[] encodedRSAKeyContent = Base64.decodeBase64(rsaKeyContent);

        rsaPrivateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(encodedRSAKeyContent));
    }

    @Override
    public PrivateKey getPrivateKey() {
        return rsaPrivateKey;
    }
}
