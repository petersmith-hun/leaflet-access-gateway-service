package hu.psprog.leaflet.lags.core.service.token.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenSettings;
import hu.psprog.leaflet.lags.core.service.util.impl.RSAKeyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for {@link JWTTokenGenerator}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class JWTTokenGeneratorTest {

    private static final OAuthConfigurationProperties O_AUTH_CONFIGURATION_PROPERTIES = prepareOAuthConfigurationProperties();
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest();
    private static final Map<String, Object> CLAIMS = prepareClaims();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JWTTokenGenerator jwtTokenGenerator;

    @BeforeEach
    public void setup() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        RSAKeyRegistry rsaKeyRegistry = new RSAKeyRegistry(O_AUTH_CONFIGURATION_PROPERTIES);
        rsaKeyRegistry.readRSAKey();
        jwtTokenGenerator = new JWTTokenGenerator(O_AUTH_CONFIGURATION_PROPERTIES, rsaKeyRegistry);
    }

    @Test
    public void shouldGenerateTokenSuccessfullyCreateJWTToken() throws IOException {

        // when
        OAuthTokenResponse result = jwtTokenGenerator.generateToken(O_AUTH_TOKEN_REQUEST, CLAIMS);

        // then
        assertToken(result.getAccessToken());
        assertThat(result.getScope(), equalTo(CLAIMS.get("scope")));
        assertThat(result.getTokenType(), equalTo("Bearer"));
        assertThat(result.getExpiresIn(), equalTo(3600));
    }

    private void assertToken(String token) throws IOException {

        String[] tokenParts = token.split("\\.");
        assertThat(tokenParts.length, equalTo(3));

        Map<String, Object> header = readTokenPart(tokenParts[0]);
        Map<String, Object> payload = readTokenPart(tokenParts[1]);
        int expectedExpiration = O_AUTH_CONFIGURATION_PROPERTIES.getToken().getExpiration();

        assertThat(header.get("alg"), equalTo("RS256"));
        assertThat(header.get("typ"), equalTo("JWT"));
        assertThat(payload.get("sub"), equalTo(CLAIMS.get("sub")));
        assertThat(payload.get("aud"), equalTo(O_AUTH_TOKEN_REQUEST.getAudience()));
        assertThat(payload.get("scope"), equalTo(CLAIMS.get("scope")));
        assertThat(payload.get("iss"), equalTo(O_AUTH_CONFIGURATION_PROPERTIES.getToken().getIssuer()));
        assertThat(payload.get("jti"), notNullValue());

        try {
            UUID.fromString(payload.get("jti").toString());
        } catch (IllegalArgumentException e) {
            fail("JTI claim is a not a valid UUID");
        }

        int expiration = (int) payload.get("exp") - (int) payload.get("iat");
        assertThat(expiration > expectedExpiration - 2 && expiration <= expectedExpiration, is(true));
    }

    private Map<String, Object> readTokenPart(String tokenPart) throws IOException {

        byte[] decodedString = Base64.getDecoder().decode(tokenPart);

        return OBJECT_MAPPER.readValue(decodedString, Map.class);
    }

    private static OAuthConfigurationProperties prepareOAuthConfigurationProperties() {

        OAuthTokenSettings oAuthTokenSettings = null;
        try {
            oAuthTokenSettings = new OAuthTokenSettings(3600, "https://oauth.dev.local:9999",
                    Paths.get(ClassLoader.getSystemResource("lags_unit_tests_jwt_rsa_prv_pkcs8.pem").toURI()));
        } catch (URISyntaxException e) {
            fail("Failed to read RSA private key for unit test", e);
        }

        return new OAuthConfigurationProperties(oAuthTokenSettings, Collections.emptyList());
    }

    private static OAuthTokenRequest prepareValidOAuthTokenRequest() {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .clientID("client1")
                .audience("audience1")
                .scope(Arrays.asList("read:all", "write:all"))
                .build();
    }

    private static Map<String, Object> prepareClaims() {

        return new HashMap<>(Map.of(
                "scope", "read:all write:all",
                "sub", "dummy-source-service-1"
        ));
    }
}
