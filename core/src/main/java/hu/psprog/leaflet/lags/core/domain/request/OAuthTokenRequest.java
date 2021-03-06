package hu.psprog.leaflet.lags.core.domain.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * OAuth2 token request model.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthTokenRequest implements OAuthRequest {

    private final GrantType grantType;
    private final String clientID;
    private final String username;
    private final String password;
    private final String audience;
    private final List<String> scope;
    private final String authorizationCode;
    private final String redirectURI;
}
