package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.service.util.SecretGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Default implementation of {@link SecretGenerator}. Implementation creates a 48-character long secure random string,
 * encoded into Base64.
 *
 * @author Peter Smith
 */
@Component
public class SecretGeneratorImpl implements SecretGenerator {

    private static final int SECURE_STRING_LENGTH = 48;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();

    @Override
    public String generateSecret() {

        byte[] randomBytes = new byte[SECURE_STRING_LENGTH];
        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }
}
