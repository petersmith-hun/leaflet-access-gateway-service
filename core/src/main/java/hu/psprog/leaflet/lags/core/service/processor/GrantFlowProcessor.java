package hu.psprog.leaflet.lags.core.service.processor;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;

import java.util.Map;

/**
 * OAuth2 compatible grant flow processor.
 * Each implementation should be able to handle a specific OAuth2 grant flow (authorization code, client credentials, etc.).
 *
 * @author Peter Smith
 */
public interface GrantFlowProcessor {

    /**
     * Executes the necessary verification steps for a given OAuth2 grant flow.
     * On success, generates and returns a map of relevant token claims that can be used to generate a JWT access token.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param oAuthClient source client descriptor based on the request
     */
    Map<String, Object> verifyRequest(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient);

    /**
     * Returns the grant type for which this implementation is compatible.
     *
     * @return compatible grant type as {@link GrantType}
     */
    GrantType forGrantType();
}
