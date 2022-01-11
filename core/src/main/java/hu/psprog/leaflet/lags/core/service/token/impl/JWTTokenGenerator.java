package hu.psprog.leaflet.lags.core.service.token.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.token.TokenGenerator;
import hu.psprog.leaflet.lags.core.service.util.KeyRegistry;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token based implementation of {@link TokenGenerator}.
 *
 * @author Peter Smith
 */
@Component
public class JWTTokenGenerator implements TokenGenerator {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;
    private final KeyRegistry keyRegistry;

    @Autowired
    public JWTTokenGenerator(OAuthConfigurationProperties oAuthConfigurationProperties, KeyRegistry keyRegistry) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        this.keyRegistry = keyRegistry;
    }

    @Override
    public OAuthTokenResponse generateToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims) {

        return OAuthTokenResponse.builder()
                .accessToken(createToken(oAuthTokenRequest, claims))
                .scope(claims.get(OAuthConstants.Request.SCOPE).toString())
                .expiresIn(oAuthConfigurationProperties.getToken().getExpiration())
                .build();
    }

    private String createToken(OAuthTokenRequest oAuthTokenRequest, Map<String, Object> claims) {

        Date issuedAt = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setAudience(oAuthTokenRequest.getAudience())
                .setExpiration(generateExpiration(issuedAt))
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(issuedAt)
                .setIssuer(oAuthConfigurationProperties.getToken().getIssuer())
                .setNotBefore(issuedAt)
                .signWith(SignatureAlgorithm.RS256, keyRegistry.getPrivateKey())
                .compact();
    }

    private Date generateExpiration(Date issuedAt) {

        Calendar calendar = new Calendar.Builder()
                .setInstant(issuedAt)
                .build();

        calendar.add(Calendar.SECOND, oAuthConfigurationProperties.getToken().getExpiration());

        return calendar.getTime();
    }
}
