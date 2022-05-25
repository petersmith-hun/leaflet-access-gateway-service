package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Domain class representing an access token's claims.
 *
 * @author Peter Smith
 */
@EqualsAndHashCode
@ToString
public class TokenClaims {

    private final Map<String, Object> claims;

    TokenClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

    public static TokenClaimsBuilder builder() {
        return new TokenClaimsBuilder();
    }

    /**
     * Returns the scope claim split by items.
     *
     * @return the scope claim split by items
     */
    public String[] getScopeAsArray() {
        return getScope().split(StringUtils.SPACE);
    }

    /**
     * Returns all claims as a map.
     * Keys of the map conform the token claim attributes defined in JWT specification.
     *
     * @return the claims as a map
     */
    public Map<String, Object> getClaimsAsMap() {
        return this.claims;
    }

    /**
     * Returns the "jti" claim value.
     *
     * @return "jti" claim value
     */
    public String getTokenID() {
        return (String) this.claims.get(OAuthConstants.Token.JTI);
    }

    /**
     * Returns the "name" claim value.
     *
     * @return "name" claim value
     */
    public String getUsername() {
        return (String) this.claims.get(OAuthConstants.Token.NAME);
    }

    /**
     * Returns the "usr" claim value.
     *
     * @return "usr" claim value
     */
    public String getEmail() {
        return (String) this.claims.get(OAuthConstants.Token.USER);
    }

    /**
     * Returns the "sub" claim value.
     *
     * @return "sub" claim value
     */
    public String getClientID() {
        return getSubject();
    }

    /**
     * Returns the "exp" claim value.
     *
     * @return "exp" claim value
     */
    public Date getExpiration() {
        return (Date) this.claims.get(OAuthConstants.Token.EXPIRATION);
    }

    /**
     * Returns the "aud" claim value.
     *
     * @return "aud" claim value
     */
    public String getAudience() {
        return (String) this.claims.get(OAuthConstants.Token.AUDIENCE);
    }

    /**
     * Returns the "scope" claim value.
     *
     * @return "scope" claim value
     */
    public String getScope() {
        return (String) this.claims.get(OAuthConstants.Token.SCOPE);
    }

    /**
     * Returns the "sub" claim value.
     *
     * @return "sub" claim value
     */
    public String getSubject() {
        return (String) this.claims.get(OAuthConstants.Token.SUBJECT);
    }

    /**
     * Returns the "rol" claim value.
     *
     * @return "role" claim value
     */
    public String getRole() {
        return (String) this.claims.get(OAuthConstants.Token.ROLE);
    }

    /**
     * Returns the "uid" claim value.
     *
     * @return "uid" claim value
     */
    public Long getUserID() {
        return (Long) this.claims.get(OAuthConstants.Token.USER_ID);
    }

    /**
     * Builder for creating a {@link TokenClaims} object containing the handled token's claims.
     */
    @ToString
    public static class TokenClaimsBuilder {

        private final Map<String, Object> claims;

        TokenClaimsBuilder() {
            this.claims = new LinkedHashMap<>();
        }

        /**
         * Adds the "jti" claim.
         *
         * @param tokenID token ID (JTI)
         * @return builder instance
         */
        public TokenClaimsBuilder tokenID(String tokenID) {
            this.claims.put(OAuthConstants.Token.JTI, tokenID);
            return this;
        }

        /**
         * Adds the "name" claim.
         *
         * @param username username of the identified user
         * @return builder instance
         */
        public TokenClaimsBuilder username(String username) {
            this.claims.put(OAuthConstants.Token.NAME, username);
            return this;
        }

        /**
         * Adds the "usr" claim.
         *
         * @param email email of the identified user
         * @return builder instance
         */
        public TokenClaimsBuilder email(String email) {
            this.claims.put(OAuthConstants.Token.USER, email);
            return this;
        }

        /**
         * Alias for "sub" (subject) claim.
         *
         * @param clientID client ID of the identified OAuth client
         * @return builder instance
         */
        public TokenClaimsBuilder clientID(String clientID) {
            return subject(clientID);
        }

        /**
         * Adds the "exp" claim.
         *
         * @param expiration token expiration date
         * @return builder instance
         */
        public TokenClaimsBuilder expiration(Date expiration) {
            this.claims.put(OAuthConstants.Token.EXPIRATION, expiration);
            return this;
        }

        /**
         * Adds the "aud" claim.
         *
         * @param audience token audience
         * @return builder instance
         */
        public TokenClaimsBuilder audience(String audience) {
            this.claims.put(OAuthConstants.Token.AUDIENCE, audience);
            return this;
        }

        /**
         * Adds the "scope" claim.
         *
         * @param scope token scope (as space-delimited String)
         * @return builder instance
         */
        public TokenClaimsBuilder scope(String scope) {
            this.claims.put(OAuthConstants.Token.SCOPE, scope);
            return this;
        }

        /**
         * Adds the "sub" claim.
         *
         * @param subject token subject
         * @return builder instance
         */
        public TokenClaimsBuilder subject(String subject) {
            this.claims.put(OAuthConstants.Token.SUBJECT, subject);
            return this;
        }

        /**
         * Adds the "rol" claim.
         *
         * @param role role of the identified user
         * @return builder instance
         */
        public TokenClaimsBuilder role(String role) {
            this.claims.put(OAuthConstants.Token.ROLE, role);
            return this;
        }

        /**
         * Adds the "uid" claim.
         *
         * @param userID user ID of the identified user
         * @return builder instance
         */
        public TokenClaimsBuilder userID(Long userID) {
            this.claims.put(OAuthConstants.Token.USER_ID, userID);
            return this;
        }

        public TokenClaims build() {
            return new TokenClaims(this.claims);
        }
    }
}
