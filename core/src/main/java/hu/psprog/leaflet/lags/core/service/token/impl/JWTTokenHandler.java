package hu.psprog.leaflet.lags.core.service.token.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.exception.JWTTokenParsingException;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private final TokenTracker tokenTracker;
    private final JwtDecoder jwtDecoder;
    private final JWSSigner jwsSigner;

    @Autowired
    public JWTTokenHandler(OAuthConfigurationProperties oAuthConfigurationProperties, TokenTracker tokenTracker,
                           JwtDecoder jwtDecoder, JWSSigner jwsSigner) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        this.tokenTracker = tokenTracker;
        this.jwtDecoder = jwtDecoder;
        this.jwsSigner = jwsSigner;
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

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);

            return TokenClaims.builder()
                    .tokenID(jwt.getId())
                    .username(String.valueOf(jwt.getClaimAsString(OAuthConstants.Token.NAME)))
                    .email(String.valueOf(jwt.getClaimAsString(OAuthConstants.Token.USER)))
                    .clientID(jwt.getSubject())
                    .scope(jwt.getClaimAsString(OAuthConstants.Token.SCOPE))
                    .expiration(Date.from(Objects.requireNonNull(jwt.getExpiresAt())))
                    .audience(jwt.getAudience()
                            .stream()
                            .findFirst()
                            .orElse(StringUtils.EMPTY))
                    .role(String.valueOf(jwt.getClaimAsString(OAuthConstants.Token.ROLE)))
                    .userID(Optional.ofNullable(jwt.getClaimAsString(OAuthConstants.Token.USER_ID))
                            .map(Long::parseLong)
                            .orElse(0L))
                    .build();
        } catch (RuntimeException exception) {
            log.error("Failed to parse access token", exception);
            throw new JWTTokenParsingException(exception);
        }
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

        JWSHeader jwsHeader = new JWSHeader.Builder(oAuthConfigurationProperties.getToken().getSignatureAlgorithm())
                .type(JOSEObjectType.JWT)
                .keyID(oAuthConfigurationProperties.getToken().getKeyID())
                .build();

        long expiresAt = convertToSeconds(storeAccessTokenInfoRequest.getExpiresAt());
        long issuedAt = convertToSeconds(storeAccessTokenInfoRequest.getIssuedAt());

        Map<String, Object> rawClaims = claims.getClaimsAsMap();
        rawClaims.put(OAuthConstants.Token.AUDIENCE, oAuthTokenRequest.getAudience());
        rawClaims.put(OAuthConstants.Token.EXPIRATION, expiresAt);
        rawClaims.put(OAuthConstants.Token.JTI, storeAccessTokenInfoRequest.getId());
        rawClaims.put(OAuthConstants.Token.ISSUED_AT, issuedAt);
        rawClaims.put(OAuthConstants.Token.ISSUER, oAuthConfigurationProperties.getToken().getIssuer());
        rawClaims.put(OAuthConstants.Token.NOT_BEFORE, issuedAt);

        Payload payload = new Payload(rawClaims);
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(jwsSigner);
        } catch (JOSEException exception) {
            log.error("Failed to sign token", exception);
            throw new OAuthTokenRequestException(OAuthErrorCode.SERVER_ERROR, exception.getMessage());
        }

        return jwsObject.serialize();
    }

    private long convertToSeconds(Date dateClaim) {
        return dateClaim.getTime() / 1000;
    }
}
