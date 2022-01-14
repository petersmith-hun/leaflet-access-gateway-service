package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
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
     * Attempts processing and authorizing the provided {@link OAuthAuthorizationRequest} sent to the OAuth2 specification
     * compatible authorization endpoint of the service. This in general initiates an Authorization Code Flow authorization process.
     *
     * @param oAuthAuthorizationRequest {@link OAuthAuthorizationRequest} object containing the authorization request parameters
     * @return generated {@link OAuthAuthorizationResponse} object on success that can be passed back to the source client application
     * @throws OAuthAuthorizationException if authorization request processing fails for any reason
     */
    OAuthAuthorizationResponse authorize(OAuthAuthorizationRequest oAuthAuthorizationRequest);

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
