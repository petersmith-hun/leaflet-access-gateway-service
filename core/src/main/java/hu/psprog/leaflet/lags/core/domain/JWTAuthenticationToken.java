package hu.psprog.leaflet.lags.core.domain;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

/**
 * Spring {@link Authentication} implementation for JWT token based authentication.
 *
 * @author Peter Smith
 */
public class JWTAuthenticationToken implements Authentication {

    private static final String JWT_AUTH_NAME = "JWT Authentication";

    private final TokenClaims claims;
    private final String rawToken;
    private final Collection<GrantedAuthority> authorities;

    private boolean authenticated = false;

    private JWTAuthenticationToken(TokenClaims claims, String rawToken) {
        // prevent instantiation
        this.claims = claims;
        this.rawToken = rawToken;
        this.authorities = AuthorityUtils.createAuthorityList(claims.getScopes());
    }

    @Override
    public String getName() {
        return JWT_AUTH_NAME;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return rawToken;
    }

    @Override
    public TokenClaims getDetails() {
        return claims;
    }

    @Override
    public Object getPrincipal() {
        return claims.getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    public static JWTAuthenticationTokenBuilder getBuilder() {
        return new JWTAuthenticationTokenBuilder();
    }

    /**
     * Builder for {@link JWTAuthenticationToken}.
     */
    public static final class JWTAuthenticationTokenBuilder {
        private TokenClaims claims;
        private String rawToken;

        private JWTAuthenticationTokenBuilder() {
        }

        public JWTAuthenticationTokenBuilder withClaims(TokenClaims claims) {
            this.claims = claims;
            return this;
        }

        public JWTAuthenticationTokenBuilder withRawToken(String rawToken) {
            this.rawToken = rawToken;
            return this;
        }

        public JWTAuthenticationToken build() {
            return new JWTAuthenticationToken(claims, rawToken);
        }
    }
}
