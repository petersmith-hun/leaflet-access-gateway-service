package hu.psprog.leaflet.lags.core.service.token.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenSettings;
import hu.psprog.leaflet.lags.core.domain.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.TokenClaims;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.util.TokenTracker;
import hu.psprog.leaflet.lags.core.service.util.impl.RSAKeyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link JWTTokenHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class JWTTokenHandlerTest {

    private static final OAuthConfigurationProperties O_AUTH_CONFIGURATION_PROPERTIES = prepareOAuthConfigurationProperties();
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest();
    private static final Map<String, Object> CLAIMS = prepareClaims();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EMAIL = "user@dev.local";
    private static final String AUDIENCE = "target-svc-aud-1";

    private JWTTokenHandler jwtTokenHandler;

    @Mock
    private TokenTracker tokenTracker;

    @Captor
    private ArgumentCaptor<StoreAccessTokenInfoRequest> storeAccessTokenInfoRequestArgumentCaptor;

    @BeforeEach
    public void setup() {
        RSAKeyRegistry rsaKeyRegistry = new RSAKeyRegistry(O_AUTH_CONFIGURATION_PROPERTIES);
        rsaKeyRegistry.readRSAKey();
        jwtTokenHandler = new JWTTokenHandler(O_AUTH_CONFIGURATION_PROPERTIES, rsaKeyRegistry, tokenTracker);
    }

    @Test
    public void shouldGenerateTokenSuccessfullyCreateJWTToken() throws IOException {

        int expirationInSeconds = O_AUTH_CONFIGURATION_PROPERTIES.getToken().getExpiration();

        // when
        OAuthTokenResponse result = jwtTokenHandler.generateToken(O_AUTH_TOKEN_REQUEST, CLAIMS);

        // then
        assertToken(result.getAccessToken(), expirationInSeconds);
        assertThat(result.getScope(), equalTo(CLAIMS.get("scope")));
        assertThat(result.getTokenType(), equalTo("Bearer"));
        assertThat(result.getExpiresIn(), equalTo(3600));
        assertStoreAccessTokenRequest(expirationInSeconds);
    }

    @Test
    public void shouldGenerateTokenSuccessfullyCreateJWTTokenWithCustomExpiration() throws IOException {

        // given
        int customExpirationInSeconds = 600;

        // when
        OAuthTokenResponse result = jwtTokenHandler.generateToken(O_AUTH_TOKEN_REQUEST, CLAIMS, customExpirationInSeconds);

        // then
        assertToken(result.getAccessToken(), customExpirationInSeconds);
        assertThat(result.getScope(), equalTo(CLAIMS.get("scope")));
        assertThat(result.getTokenType(), equalTo("Bearer"));
        assertThat(result.getExpiresIn(), equalTo(customExpirationInSeconds));
        assertStoreAccessTokenRequest(customExpirationInSeconds);
    }

    @Test
    public void shouldParseTokenExtractClaims() {

        // given
        OAuthTokenResponse tokenResponse = jwtTokenHandler.generateToken(O_AUTH_TOKEN_REQUEST, CLAIMS);

        // when
        TokenClaims result = jwtTokenHandler.parseToken(tokenResponse.getAccessToken());

        // then
        verifyJTI(result.getTokenID());
        assertThat(System.currentTimeMillis() - result.getExpiration().getTime() < 1000, is(true));
        assertThat(result.getClientID(), equalTo(CLAIMS.get("sub")));
        assertThat(result.getUsername(), equalTo("null"));
        assertThat(result.getEmail(), equalTo(EMAIL));
        assertThat(result.getAudience(), equalTo(AUDIENCE));
        assertThat(result.getScopes(), equalTo(new String[] {"read:all", "write:all"}));
    }

    @Test
    public void shouldParseTokenThrowAuthenticationExceptionOnInvalidToken() {

        // given
        OAuthTokenResponse tokenResponse = jwtTokenHandler.generateToken(O_AUTH_TOKEN_REQUEST, CLAIMS);
        String invalidToken = tokenResponse.getAccessToken().substring(10);

        // when
        Throwable result = assertThrows(AuthenticationException.class, () -> jwtTokenHandler.parseToken(invalidToken));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Failed to parse JWT token"));
    }

    private void assertToken(String token, int expectedExpiration) throws IOException {

        String[] tokenParts = token.split("\\.");
        assertThat(tokenParts.length, equalTo(3));

        Map<String, Object> header = readTokenPart(tokenParts[0]);
        Map<String, Object> payload = readTokenPart(tokenParts[1]);

        assertThat(header.get("alg"), equalTo("RS256"));
        assertThat(header.get("typ"), equalTo("JWT"));
        assertThat(payload.get("sub"), equalTo(CLAIMS.get("sub")));
        assertThat(payload.get("aud"), equalTo(O_AUTH_TOKEN_REQUEST.getAudience()));
        assertThat(payload.get("scope"), equalTo(CLAIMS.get("scope")));
        assertThat(payload.get("iss"), equalTo(O_AUTH_CONFIGURATION_PROPERTIES.getToken().getIssuer()));
        assertThat(payload.get("jti"), notNullValue());
        verifyJTI(payload.get("jti").toString());

        int expiration = (int) payload.get("exp") - (int) payload.get("iat");
        assertThat(expiration > expectedExpiration - 2 && expiration <= expectedExpiration, is(true));
    }

    private void assertStoreAccessTokenRequest(int expirationInSeconds) {

        verify(tokenTracker).storeTokenInfo(storeAccessTokenInfoRequestArgumentCaptor.capture());

        StoreAccessTokenInfoRequest request = storeAccessTokenInfoRequestArgumentCaptor.getValue();
        verifyJTI(request.getId());
        assertThat(request.getSubject(), equalTo(CLAIMS.get("sub")));
        assertThat(request.getExpiresAt(), notNullValue());
        assertThat(request.getIssuedAt(), notNullValue());
        assertThat(request.getExpiresAt().getTime() - request.getIssuedAt().getTime() == expirationInSeconds * 1000L, is(true));
    }

    private void verifyJTI(String jti) {
        assertThat(jti, notNullValue());
        try {
            UUID.fromString(jti);
        } catch (IllegalArgumentException e) {
            fail("JTI claim is a not a valid UUID");
        }
    }

    private Map<String, Object> readTokenPart(String tokenPart) throws IOException {

        byte[] decodedString = Base64.getDecoder().decode(tokenPart);

        return OBJECT_MAPPER.readValue(decodedString, Map.class);
    }

    private static OAuthConfigurationProperties prepareOAuthConfigurationProperties() {

        OAuthTokenSettings oAuthTokenSettings = null;
        try {
            oAuthTokenSettings = new OAuthTokenSettings(3600, "https://oauth.dev.local:9999",
                    Paths.get(ClassLoader.getSystemResource("lags_unit_tests_jwt_rsa_prv_pkcs8.pem").toURI()),
                    Paths.get(ClassLoader.getSystemResource("lags_unit_tests_jwt_rsa_pub.pem").toURI()));
        } catch (URISyntaxException e) {
            fail("Failed to read RSA private key for unit test", e);
        }

        return new OAuthConfigurationProperties(oAuthTokenSettings, Collections.emptyList());
    }

    private static OAuthTokenRequest prepareValidOAuthTokenRequest() {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .clientID("client1")
                .audience(AUDIENCE)
                .scope(Arrays.asList("read:all", "write:all"))
                .build();
    }

    private static Map<String, Object> prepareClaims() {

        return new HashMap<>(Map.of(
                "scope", "read:all write:all",
                "sub", "dummy-source-service-1",
                "usr", EMAIL
        ));
    }
}
