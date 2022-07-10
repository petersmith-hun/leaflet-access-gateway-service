package hu.psprog.leaflet.lags.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Model class for server meta-information responses.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class AuthServerMetaInfo {

    private final String issuer;

    @JsonProperty("authorization_endpoint")
    private final String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private final String tokenEndpoint;

    @JsonProperty("jwks_uri")
    private final String jwksURI;

    @JsonProperty("token_introspection_endpoint")
    private final String tokenIntrospectionEndpoint;

    @JsonProperty("userinfo_endpoint")
    private final String userinfoEndpoint;

    @JsonProperty("grant_types_supported")
    private final List<String> grantTypesSupported;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private final List<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("response_types_supported")
    private final List<String> responseTypesSupported;
}
