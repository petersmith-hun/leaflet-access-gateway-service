package hu.psprog.leaflet.lags.core.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * OAuth2 token response model.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthTokenResponse {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("expires_in")
    private final int expiresIn;

    private final String scope;

    @JsonProperty("token_type")
    private final String tokenType = "Bearer";
}
