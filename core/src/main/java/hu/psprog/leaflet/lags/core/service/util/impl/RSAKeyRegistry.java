package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.service.util.KeyRegistry;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    private PublicKey rsaPublicKey;

    @Autowired
    public RSAKeyRegistry(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    @PostConstruct
    public void readRSAKey() {
        rsaPrivateKey = loadPrivateKey();
        rsaPublicKey = loadPublicKey();
    }

    @Override
    public PrivateKey getPrivateKey() {
        return rsaPrivateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return rsaPublicKey;
    }

    private PrivateKey loadPrivateKey() {

        return loadKey(oAuthConfigurationProperties.getToken().getPrivateKeyFile(), (keyFactory, pkcs8EncodedKeySpec) -> {
            try {
                return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            } catch (InvalidKeySpecException e) {
                throw new IllegalArgumentException("Failed to generate private key from key file", e);
            }
        }, PKCS8EncodedKeySpec::new);
    }

    private PublicKey loadPublicKey() {

        return loadKey(oAuthConfigurationProperties.getToken().getPublicKeyFile(), (keyFactory, pkcs8EncodedKeySpec) -> {
            try {
                return keyFactory.generatePublic(pkcs8EncodedKeySpec);
            } catch (InvalidKeySpecException e) {
                throw new IllegalArgumentException("Failed to generate public key from key file", e);
            }
        }, X509EncodedKeySpec::new);
    }

    private <K extends Key> K loadKey(Path keyFile, BiFunction<KeyFactory, KeySpec, K> keyGeneratorFunction, Function<byte[], KeySpec> keySpecFunction) {

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedRSAKeyContent = getEncodedRSAKeyContent(keyFile);
            KeySpec keySpec = keySpecFunction.apply(encodedRSAKeyContent);

            return keyGeneratorFunction.apply(keyFactory, keySpec);

        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException(String.format("Failed to load RSA keyfile=[%s]", keyFile.getFileName()));
        }
    }

    private byte[] getEncodedRSAKeyContent(Path keyFilePath) throws IOException {

        List<String> rsaKeyLines = Files.readAllLines(keyFilePath);
        String rsaKeyContent = rsaKeyLines.stream()
                .limit(rsaKeyLines.size() - 1)
                .skip(1)
                .collect(Collectors.joining());

        return Base64.decodeBase64(rsaKeyContent);
    }
}
