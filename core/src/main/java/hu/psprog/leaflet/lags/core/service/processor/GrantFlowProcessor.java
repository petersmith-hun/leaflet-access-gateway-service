package hu.psprog.leaflet.lags.core.service.processor;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

/**
 * OAuth2 compatible grant flow processor.
 * Each implementation should be able to handle a specific OAuth2 grant flow (authorization code, client credentials, etc.).
 *
 * @author Peter Smith
 */
public interface GrantFlowProcessor {

    /**
     * Executes the necessary verification and processing steps for OAuth2 Authorization Code Flow based authorization.
     * On success, generates an {@link OAuthAuthorizationResponse} object containing the generated authorization code,
     * the received state value and redirection URL. Internally it also stores some meta-information about the authorization
     * request, so the access token can be generated based on those in the next step of process.
     *
     * @param context {@link OAuthAuthorizationRequestContext} object containing the authorization request parameters
     * @return generated {@link OAuthAuthorizationResponse} object to be sent back to the source client application
     * @throws OAuthAuthorizationException in case of errors during processing
     */
    OAuthAuthorizationResponse processAuthorizationRequest(OAuthAuthorizationRequestContext context);

    /**
     * Executes the necessary verification steps for a given OAuth2 grant flow.
     * On success, generates and returns a map of relevant token claims that can be used to generate a JWT access token.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     * @throws OAuthAuthorizationException in case of errors during processing
     */
    TokenClaims processTokenRequest(OAuthTokenRequestContext context);

    /**
     * Returns the grant type for which this implementation is compatible.
     *
     * @return compatible grant type as {@link GrantType}
     */
    GrantType forGrantType();
}
