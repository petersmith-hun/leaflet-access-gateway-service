package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult;
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

    /**
     * Verifies if the given token is followed by LAGS.
     * If so, the status and some additional information of the token are returned.
     * Otherwise, the result object always indicates an inactive token.
     *
     * @param accessToken the token to be introspected
     * @return introspection result as {@link TokenIntrospectionResult}
     */
    TokenIntrospectionResult introspect(String accessToken);
}
