package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;

/**
 * OAuth2 authorization code flow response model.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthAuthorizationResponse {

    private final String redirectURI;
    private final String code;
    private final String state;
}
