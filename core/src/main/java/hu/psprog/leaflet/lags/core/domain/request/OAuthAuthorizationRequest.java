package hu.psprog.leaflet.lags.core.domain.request;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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

    public String[] getScopeAsArray() {
        return scope.split(StringUtils.SPACE);
    }
}
