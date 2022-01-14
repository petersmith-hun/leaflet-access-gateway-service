package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth2 authorization request model.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthAuthorizationRequest implements OAuthRequest {

    private final AuthorizationResponseType responseType;
    private final String clientID;
    private final String redirectURI;
    private final String scope;
    private final String state;

    @Override
    public GrantType getGrantType() {
        return GrantType.AUTHORIZATION_CODE;
    }
}
