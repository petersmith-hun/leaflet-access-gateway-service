package hu.psprog.leaflet.lags.core.service.token;

import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.exception.JWTTokenParsingException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Handler for generating and parsing tokens OAuth2 access tokens.
 * Implementations should be able to generate an OAuth2 compatible access token and wrap them as {@link OAuthTokenResponse}
 * object, that is supposed to be converted to an OAuth2 compatible token response. Implementations also must be able to
 * parse a given OAuth2 compatible token.
 *
 * @author Peter Smith
 */
public interface TokenHandler {

    /**
     * Generates an OAuth2 access token based on the given {@link OAuthTokenRequest} and the formerly generated custom claims.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param claims custom token claims as {@link TokenClaims}
     * @return generated access token wrapped as {@link OAuthTokenResponse}
     */
    OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims);

    /**
     * Generates an OAuth2 access token based on the given {@link OAuthTokenRequest} and the formerly generated custom claims.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param claims custom token claims as {@link TokenClaims}
     * @param customExpirationInSeconds expiration time in seconds to override the default value
     * @return generated access token wrapped as {@link OAuthTokenResponse}
     */
    OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims, int customExpirationInSeconds);

    /**
     * Parses the given access token. On success, returns the payload contents of the token (i.e. the claims).
     *
     * @param accessToken the access token to be parsed
     * @return the extracted claims as {@link TokenClaims}
     * @throws JWTTokenParsingException when the token cannot be parsed for some reason
     */
    TokenClaims parseToken(String accessToken);

    /**
     * Returns the payload contents of the already resolved token (i.e. the claims).
     *
     * @param jwt the {@link Jwt} representation of the access token to extract claims from
     * @return extracted claims as {@link TokenClaims}
     */
    TokenClaims extractClaims(Jwt jwt);
}
