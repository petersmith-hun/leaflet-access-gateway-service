package hu.psprog.leaflet.lags.core.service.token.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.registry.KeyRegistry;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token based implementation of {@link TokenHandler}.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
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
    public OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims) {
        return generateToken(oAuthTokenRequest, claims, oAuthConfigurationProperties.getToken().getExpiration());
    }

    @Override
    public OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims, int customExpirationInSeconds) {

        return OAuthTokenResponse.builder()
                .accessToken(createToken(oAuthTokenRequest, claims, customExpirationInSeconds))
                .scope(claims.getScope())
                .expiresIn(customExpirationInSeconds)
                .build();
    }

    @Override
    public TokenClaims parseToken(String accessToken) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyRegistry.getPublicKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        return TokenClaims.builder()
                .tokenID(claims.getId())
                .username(String.valueOf(claims.get(OAuthConstants.Token.NAME)))
                .email(String.valueOf(claims.get(OAuthConstants.Token.USER)))
                .clientID(claims.get(OAuthConstants.Token.SUBJECT).toString())
                .scope(claims.get(OAuthConstants.Token.SCOPE).toString())
                .expiration(claims.getExpiration())
                .audience(claims.getAudience())
                .role(String.valueOf(claims.get(OAuthConstants.Token.ROLE)))
                .userID(Long.parseLong(claims.getOrDefault(OAuthConstants.Token.USER_ID, "0").toString()))
                .build();
    }

    private String createToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims, int expirationInSeconds) {

        StoreAccessTokenInfoRequest storeAccessTokenInfoRequest = createAccessTokenInfoRequest(claims, expirationInSeconds);
        String accessToken = createAccessToken(oAuthTokenRequest, claims, storeAccessTokenInfoRequest);

        log.info("Access token issued for client={} with JTI={}", oAuthTokenRequest.getClientID(), storeAccessTokenInfoRequest.getId());
        tokenTracker.storeTokenInfo(storeAccessTokenInfoRequest);

        return accessToken;
    }

    private StoreAccessTokenInfoRequest createAccessTokenInfoRequest(TokenClaims claims, int expirationInSeconds) {

        Date issuedAt = new Date();

        return StoreAccessTokenInfoRequest.builder()
                .id(UUID.randomUUID().toString())
                .subject(claims.getSubject())
                .issuedAt(issuedAt)
                .expiresAt(generateExpiration(issuedAt, expirationInSeconds))
                .build();
    }

    private Date generateExpiration(Date issuedAt, int expirationInSeconds) {

        Calendar calendar = new Calendar.Builder()
                .setInstant(issuedAt)
                .build();

        calendar.add(Calendar.SECOND, expirationInSeconds);

        return calendar.getTime();
    }

    private String createAccessToken(OAuthTokenRequest oAuthTokenRequest, TokenClaims claims, StoreAccessTokenInfoRequest storeAccessTokenInfoRequest) {

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setHeaderParam(JwsHeader.KEY_ID, oAuthConfigurationProperties.getToken().getKeyID())
                .setClaims(claims.getClaimsAsMap())
                .setAudience(oAuthTokenRequest.getAudience())
                .setExpiration(storeAccessTokenInfoRequest.getExpiresAt())
                .setId(storeAccessTokenInfoRequest.getId())
                .setIssuedAt(storeAccessTokenInfoRequest.getIssuedAt())
                .setIssuer(oAuthConfigurationProperties.getToken().getIssuer())
                .setNotBefore(storeAccessTokenInfoRequest.getIssuedAt())
                .signWith(keyRegistry.getPrivateKey(), oAuthConfigurationProperties.getToken().getSignatureAlgorithm())
                .compact();
    }
}
