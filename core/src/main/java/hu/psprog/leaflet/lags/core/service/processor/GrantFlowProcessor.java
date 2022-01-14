package hu.psprog.leaflet.lags.core.service.processor;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

import java.util.Map;

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
     * @param oAuthAuthorizationRequest {@link OAuthAuthorizationRequest} object containing the authorization request parameters
     * @param oAuthClient {@link OAuthClient} registration object for the source client application
     * @return generated {@link OAuthAuthorizationResponse} object to be sent back to the source client application
     * @throws OAuthAuthorizationException in case of errors during processing
     */
    OAuthAuthorizationResponse authorizeRequest(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient);

    /**
     * Executes the necessary verification steps for a given OAuth2 grant flow.
     * On success, generates and returns a map of relevant token claims that can be used to generate a JWT access token.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param oAuthClient source client descriptor based on the request
     * @throws OAuthAuthorizationException in case of errors during processing
     */
    Map<String, Object> verifyRequest(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient);

    /**
     * Returns the grant type for which this implementation is compatible.
     *
     * @return compatible grant type as {@link GrantType}
     */
    GrantType forGrantType();
}
