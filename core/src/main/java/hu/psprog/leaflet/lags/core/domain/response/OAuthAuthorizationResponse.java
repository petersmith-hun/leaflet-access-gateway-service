package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;

/**
 * OAuth2 authorization code flow response model.
 *
 * @author Peter Smith
 */
@Builder
public record OAuthAuthorizationResponse(
        String redirectURI,
        String code,
        String state
) { }
