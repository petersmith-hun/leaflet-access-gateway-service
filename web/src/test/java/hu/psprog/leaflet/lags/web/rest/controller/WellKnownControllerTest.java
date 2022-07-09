package hu.psprog.leaflet.lags.web.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import hu.psprog.leaflet.lags.web.model.AuthServerMetaInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link WellKnownController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class WellKnownControllerTest {

    private static final Map<String, Object> JWKS_RESPONSE = Map.of("kid", "unit-test-key");
    private static final String ISSUER = "http://localhost:9999";
    private static final AuthServerMetaInfo AUTH_SERVER_META_INFO = prepareAuthServerMetaInfo();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {};

    @Mock
    private JWKSet jwkSet;

    private ObjectMapper objectMapper;

    private WellKnownController wellKnownController;

    @BeforeEach
    public void setup() {
        wellKnownController = new WellKnownController(jwkSet, AUTH_SERVER_META_INFO);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldGetJWKsReturnJWKSet() {

        // given
        given(jwkSet.toJSONObject()).willReturn(JWKS_RESPONSE);

        // when
        ResponseEntity<Map<String, Object>> result = wellKnownController.getJWKs();

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), notNullValue());
        assertThat(result.getBody(), equalTo(JWKS_RESPONSE));
    }

    @Test
    public void shouldGetServerMetaInfoReturnExposedMetaInfo() throws JsonProcessingException {

        // when
        ResponseEntity<AuthServerMetaInfo> result = wellKnownController.getServerMetaInfo();

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), equalTo(AUTH_SERVER_META_INFO));
        assertResultAsJSON(result.getBody());
    }

    private void assertResultAsJSON(AuthServerMetaInfo authServerMetaInfo) throws JsonProcessingException {

        String jsonString = objectMapper.writeValueAsString(authServerMetaInfo);
        Map<String, Object> deserialized = objectMapper.readValue(jsonString, MAP_TYPE_REFERENCE);

        assertThat(deserialized, equalTo(Map.of(
                "issuer", "http://localhost:9999",
                "authorization_endpoint", "http://localhost:9999/authorize",
                "token_endpoint", "http://localhost:9999/token",
                "jwks_uri", "http://localhost:9999/jwks",
                "token_introspection_endpoint", "http://localhost:9999/introspect",
                "grant_types_supported", List.of("authorization_code","client_credentials"),
                "token_endpoint_auth_methods_supported", List.of("client_secret_post","client_secret_basic"),
                "response_types_supported", List.of("code")
        )));
    }

    private static AuthServerMetaInfo prepareAuthServerMetaInfo() {

        return AuthServerMetaInfo.builder()
                .issuer(ISSUER)
                .authorizationEndpoint(ISSUER + "/authorize")
                .tokenEndpoint(ISSUER + "/token")
                .jwksURI(ISSUER + "/jwks")
                .tokenIntrospectionEndpoint(ISSUER + "/introspect")
                .responseTypesSupported(List.of("code"))
                .grantTypesSupported(List.of("authorization_code", "client_credentials"))
                .tokenEndpointAuthMethodsSupported(List.of("client_secret_post", "client_secret_basic"))
                .build();
    }
}
