package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

/**
 * OAuth2 authorization related operations.
 *
 * @author Peter Smith
 */
public interface OAuthAuthorizationService {

    /**
     * Attempts processing and authorizing the provided {@link OAuthTokenRequest} sent to the OAuth2 specification
     * compatible token endpoint of the service.
     *
     * @param oAuthTokenRequest {@link OAuthTokenRequest} object containing the token request parameters
     * @return generated access token wrapped in OAuth2 specification compatible {@link OAuthTokenResponse} object
     * @throws OAuthAuthorizationException if token request processing fails for any reason
     */
    OAuthTokenResponse authorize(OAuthTokenRequest oAuthTokenRequest);
}
