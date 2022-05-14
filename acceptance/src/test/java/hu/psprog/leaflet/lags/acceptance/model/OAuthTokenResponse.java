package hu.psprog.leaflet.lags.acceptance.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * OAuth2 token response model.
 *
 * @author Peter Smith
 */
@Data
public class OAuthTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    // this is only for error messages
    private String message;
}
