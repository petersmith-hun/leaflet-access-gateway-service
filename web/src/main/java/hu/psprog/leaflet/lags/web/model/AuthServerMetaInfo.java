package hu.psprog.leaflet.lags.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

/**
 * Model class for server meta-information responses.
 *
 * @author Peter Smith
 */
@Builder
public record AuthServerMetaInfo(
        String issuer,
        @JsonProperty("authorization_endpoint") String authorizationEndpoint,
        @JsonProperty("token_endpoint") String tokenEndpoint,
        @JsonProperty("jwks_uri") String jwksURI,
        @JsonProperty("token_introspection_endpoint") String tokenIntrospectionEndpoint,
        @JsonProperty("userinfo_endpoint") String userinfoEndpoint,
        @JsonProperty("grant_types_supported") List<String> grantTypesSupported,
        @JsonProperty("token_endpoint_auth_methods_supported") List<String> tokenEndpointAuthMethodsSupported,
        @JsonProperty("response_types_supported") List<String> responseTypesSupported
) { }
