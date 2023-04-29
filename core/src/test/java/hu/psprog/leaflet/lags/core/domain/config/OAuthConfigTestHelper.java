package hu.psprog.leaflet.lags.core.domain.config;

import com.nimbusds.jose.JWSAlgorithm;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test utility class for {@link GrantFlowProcessor} implementation tests.
 *
 * @author Peter Smith
 */
public class OAuthConfigTestHelper {

    public static final OAuthClient SOURCE_O_AUTH_CLIENT = prepareSourceOAuthClient();
    public static final OAuthClient TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(true);
    public static final OAuthClient INVALID_TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(false);
    public static final String KEY_ID = "unit-test-key";

    public static OAuthClient prepareOAuthClient(String clientName, ApplicationType applicationType, String clientID, String clientSecret, String audience) {

        OAuthClient oAuthClient = new OAuthClient();
        oAuthClient.setClientName(clientName);
        oAuthClient.setApplicationType(applicationType);
        oAuthClient.setClientId(clientID);
        oAuthClient.setClientSecret(clientSecret);
        oAuthClient.setAudience(audience);

        return oAuthClient;
    }

    public static OAuthClientAllowRelation prepareRelation(String clientName, List<String> allowedScopes) {

        OAuthClientAllowRelation relation = new OAuthClientAllowRelation();
        relation.setName(clientName);
        relation.setAllowedScopes(allowedScopes);

        return relation;
    }

    public static OAuthConfigurationProperties prepareConfig(OAuthTokenSettings oAuthTokenSettings, Duration authCodeExpiration, List<OAuthClient> oAuthClients) {

        OAuthConfigurationProperties oAuthConfigurationProperties = new OAuthConfigurationProperties();
        oAuthConfigurationProperties.setToken(oAuthTokenSettings);
        oAuthConfigurationProperties.setAuthCodeExpiration(authCodeExpiration);
        oAuthConfigurationProperties.setClients(oAuthClients);

        return oAuthConfigurationProperties;
    }

    public static OAuthTokenSettings prepareTokenSettings(int expiration, String issuer, Path privateKeyfile, Path publicKeyFile) {

        OAuthTokenSettings oAuthTokenSettings = new OAuthTokenSettings();
        oAuthTokenSettings.setExpiration(expiration);
        oAuthTokenSettings.setIssuer(issuer);
        oAuthTokenSettings.setPrivateKeyFile(privateKeyfile);
        oAuthTokenSettings.setPublicKeyFile(publicKeyFile);
        oAuthTokenSettings.setSignatureAlgorithm(JWSAlgorithm.RS256);
        oAuthTokenSettings.setKeyID(KEY_ID);

        return oAuthTokenSettings;
    }

    public static void setRegisteredScopes(OAuthClient oAuthClient, List<String> registeredScopes) {
        oAuthClient.setRegisteredScopes(registeredScopes);
    }

    public static void setRequiredScopes(OAuthClient oAuthClient, List<String> requiredScopes) {
        oAuthClient.setRequiredScopes(requiredScopes);
    }

    public static void setAllowedClients(OAuthClient oAuthClient, List<OAuthClientAllowRelation> allowedClients) {
        oAuthClient.setAllowedClients(allowedClients);
    }

    public static void setAllowedCallbacks(OAuthClient oAuthClient, List<String> allowedCallbacks) {
        oAuthClient.setAllowedCallbacks(allowedCallbacks);
    }

    private static OAuthClient prepareSourceOAuthClient() {

        OAuthClient oAuthClient = new OAuthClient();
        oAuthClient.setClientName("source-service-1");
        oAuthClient.setApplicationType(ApplicationType.SERVICE);
        oAuthClient.setClientId("dummy-source-service-1");
        oAuthClient.setClientSecret("secret1");
        oAuthClient.setAudience("source-service-audience");

        return oAuthClient;
    }

    private static OAuthClient prepareTargetOAuthClient(boolean validRelation) {

        OAuthClientAllowRelation relation = new OAuthClientAllowRelation();
        relation.setName(validRelation ? "source-service-1" : "source-service-2");
        relation.setAllowedScopes(Arrays.asList("read:items", "write:item:self", "default1", "default2", "default3"));

        OAuthClient oAuthClient = new OAuthClient();
        oAuthClient.setClientName("target-service-1");
        oAuthClient.setApplicationType(ApplicationType.SERVICE);
        oAuthClient.setClientId("dummy-target-service-1");
        oAuthClient.setClientSecret("secret2");
        oAuthClient.setAudience("target-service-audience");
        oAuthClient.setRegisteredScopes(Arrays.asList("read:items", "write:item:all", "write:item:self", "admin:item", "default1", "default2", "default3"));
        oAuthClient.setAllowedClients(Collections.singletonList(relation));

        return oAuthClient;
    }
}
