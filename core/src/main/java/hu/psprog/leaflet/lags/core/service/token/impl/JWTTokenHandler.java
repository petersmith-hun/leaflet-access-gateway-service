package hu.psprog.leaflet.lags.core.service.token.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.TokenClaims;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.util.KeyRegistry;
import hu.psprog.leaflet.lags.core.service.util.TokenTracker;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token based implementation of {@link TokenHandler}.
 *
 * @author Peter Smith
 */
@Component
public class JWTTokenHandler implements TokenHandler {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;
    private final KeyRegistry keyRegistry;
    private final TokenTracker tokenTracker;

    @Autowired
    public JWTTokenHandler(OAuthConfigurationProperties oAuthConfigurationProperties, KeyRegistry keyRegistry, TokenTracker tokenTracker) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        this.keyRegistry = keyRegistry;
        this.tokenTracker = tokenTracker;
    }

    @Override
    public OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims) {
        return generateToken(oAuthTokenRequest, claims, oAuthConfigurationProperties.getToken().getExpiration());
    }

    @Override
    public OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims, int customExpirationInSeconds) {

        return OAuthTokenResponse.builder()
                .accessToken(createToken(oAuthTokenRequest, claims, customExpirationInSeconds))
                .scope(claims.get(OAuthConstants.Request.SCOPE).toString())
                .expiresIn(customExpirationInSeconds)
                .build();
    }

    @Override
    public TokenClaims parseToken(String accessToken) {

        try {

            Claims claims = Jwts.parser()
                    .setSigningKey(keyRegistry.getPublicKey())
                    .parseClaimsJws(accessToken)
                    .getBody();

            return TokenClaims.builder()
                    .tokenID(claims.getId())
                    .username(String.valueOf(claims.get(OAuthConstants.Token.NAME)))
                    .email(String.valueOf(claims.get(OAuthConstants.Token.USER)))
                    .clientID(claims.get(OAuthConstants.Token.SUBJECT).toString())
                    .scopes(extractScopes(claims))
                    .expiration(claims.getExpiration())
                    .audience(claims.getAudience())
                    .build();
        } catch (JwtException e) {
            throw new AuthenticationException("Failed to parse JWT token", e);
        }
    }

    private String createToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims, int expirationInSeconds) {

        Date issuedAt = new Date();
        StoreAccessTokenInfoRequest storeAccessTokenInfoRequest = StoreAccessTokenInfoRequest.builder()
                .id(UUID.randomUUID().toString())
                .subject(claims.get(OAuthConstants.Token.SUBJECT).toString())
                .issuedAt(issuedAt)
                .expiresAt(generateExpiration(issuedAt, expirationInSeconds))
                .build();

        tokenTracker.storeTokenInfo(storeAccessTokenInfoRequest);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .setAudience(oAuthTokenRequest.getAudience())
                .setExpiration(storeAccessTokenInfoRequest.getExpiresAt())
                .setId(storeAccessTokenInfoRequest.getId())
                .setIssuedAt(storeAccessTokenInfoRequest.getIssuedAt())
                .setIssuer(oAuthConfigurationProperties.getToken().getIssuer())
                .setNotBefore(storeAccessTokenInfoRequest.getIssuedAt())
                .signWith(SignatureAlgorithm.RS256, keyRegistry.getPrivateKey())
                .compact();
    }

    private Date generateExpiration(Date issuedAt, int expirationInSeconds) {

        Calendar calendar = new Calendar.Builder()
                .setInstant(issuedAt)
                .build();

        calendar.add(Calendar.SECOND, expirationInSeconds);

        return calendar.getTime();
    }

    private String[] extractScopes(Claims claims) {

        return claims.get(OAuthConstants.Token.SCOPE)
                .toString()
                .split(StringUtils.SPACE);
    }
}
