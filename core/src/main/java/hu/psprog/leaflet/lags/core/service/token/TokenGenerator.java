package hu.psprog.leaflet.lags.core.service.token;

import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;

import java.util.Map;

/**
 * OAuth2 access token generator.
 * Implementations should be able to generate an OAuth2 compatible access token and wrap them as {@link OAuthTokenResponse}
 * object, that is supposed to be converted to an OAuth2 compatible token response.
 *
 * @author Peter Smith
 */
public interface TokenGenerator {

    /**
     * Generates an OAuth2 access token based on the given {@link OAuthTokenRequest} and the formerly generated custom claims.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param claims custom token claims as map
     * @return generated access token wrapped as {@link OAuthTokenResponse}
     */
    OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims);
}
