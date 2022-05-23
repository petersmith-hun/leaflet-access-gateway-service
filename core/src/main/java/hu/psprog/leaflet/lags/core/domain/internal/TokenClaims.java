package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Domain class representing an access token's claims.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class TokenClaims {

    private static final Map<String, Function<TokenClaims, Object>> CLAIM_MAPPING = createClaimMapping();

    private final String tokenID;
    private final String username;
    private final String email;
    private final String clientID;
    private final Date expiration;
    private final String audience;
    private final String scope;
    private final String subject;
    private final String role;
    private final Long userID;

    /**
     * Returns the scope claim split by items.
     *
     * @return the scope claim split by items
     */
    public String[] getScopeAsArray() {
        return scope.split(StringUtils.SPACE);
    }

    /**
     * Returns the claims as a map.
     * Keys of the map conform the token claim attributes defined in JWT specification.
     *
     * @return the claims as a map
     */
    public Map<String, Object> getClaimsAsMap() {

        return CLAIM_MAPPING.entrySet().stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().apply(this)))
                .filter(pair -> Objects.nonNull(pair.getValue()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private static Map<String, Function<TokenClaims, Object>> createClaimMapping() {

        Map<String, Function<TokenClaims, Object>> claimMapping = new LinkedHashMap<>();
        claimMapping.put(OAuthConstants.Token.SCOPE, TokenClaims::getScope);
        claimMapping.put(OAuthConstants.Token.SUBJECT, TokenClaims::getSubject);
        claimMapping.put(OAuthConstants.Token.USER, TokenClaims::getEmail);
        claimMapping.put(OAuthConstants.Token.ROLE, TokenClaims::getRole);
        claimMapping.put(OAuthConstants.Token.NAME, TokenClaims::getUsername);
        claimMapping.put(OAuthConstants.Token.USER_ID, TokenClaims::getUserID);

        return claimMapping;
    }
}
