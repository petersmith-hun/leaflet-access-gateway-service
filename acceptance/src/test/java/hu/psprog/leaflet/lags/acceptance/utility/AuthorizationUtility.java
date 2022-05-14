package hu.psprog.leaflet.lags.acceptance.utility;

import hu.psprog.leaflet.lags.acceptance.model.TestConstants;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Authorization related utilities.
 *
 * @author Peter Smith
 */
public class AuthorizationUtility {

    private AuthorizationUtility() {
    }

    /**
     * Generates a basic authorization header for OAuth client authorization.
     *
     * @return generated authorization header value in "Basic &lt;Base64-encoded-credentials>" format
     */
    public static Optional<String> generateOAuthBasicAuthorization() {

        String authorizationHeader = null;

        if (Objects.nonNull(ThreadLocalDataRegistry.get(TestConstants.Attribute.CLIENT_SECRET))) {
            String plainCredentials = String.format("%s:%s",
                    ThreadLocalDataRegistry.get(TestConstants.Attribute.CLIENT_ID),
                    ThreadLocalDataRegistry.get(TestConstants.Attribute.CLIENT_SECRET));

            authorizationHeader = generateBasicAuthorization(plainCredentials);
        }

        return Optional.ofNullable(authorizationHeader);
    }

    /**
     * Generates a basic authorization header for user authorization.
     *
     * @param email email address of the user
     * @param password password of the user
     * @return generated authorization header value in "Basic &lt;Base64-encoded-credentials>" format
     */
    public static String generateUserBasicAuthorization(String email, String password) {

        String plainCredentials = String.format("%s:%s", email, password);

        return generateBasicAuthorization(plainCredentials);
    }

    private static String generateBasicAuthorization(String plainCredentials) {
        return String.format("Basic %s", Base64.getEncoder().encodeToString(plainCredentials.getBytes()));
    }
}
