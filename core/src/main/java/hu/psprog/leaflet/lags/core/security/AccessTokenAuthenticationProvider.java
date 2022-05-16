package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.exception.RevokedTokenException;
import hu.psprog.leaflet.lags.core.service.util.TokenTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * Spring {@link AuthenticationProvider} implementation for JWT token based authentication.
 *
 * @author Peter Smith
 */
@Component
public class AccessTokenAuthenticationProvider implements AuthenticationProvider {

    private final TokenTracker tokenTracker;
    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public AccessTokenAuthenticationProvider(TokenTracker tokenTracker, AuthenticationConfig authenticationConfig) {
        this.tokenTracker = tokenTracker;
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) authentication;
        tokenTracker.retrieveTokenInfo(jwtAuthenticationToken.getDetails().getTokenID())
                .filter(activeTokenPredicate())
                .filter(reclaimAuthorityOnlyPredicate(jwtAuthenticationToken))
                .filter(validAudiencePredicate(jwtAuthenticationToken))
                .orElseThrow(() -> new RevokedTokenException(String.format("Token for user [%s] has already been revoked", authentication.getPrincipal())));

        authentication.setAuthenticated(true);

        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JWTAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Predicate<AccessTokenInfo> activeTokenPredicate() {
        return accessTokenInfo -> accessTokenInfo.getStatus() == TokenStatus.ACTIVE;
    }

    private Predicate<AccessTokenInfo> reclaimAuthorityOnlyPredicate(JWTAuthenticationToken jwtAuthenticationToken) {

        return accessTokenInfo -> jwtAuthenticationToken.getAuthorities().size() == 1
                && jwtAuthenticationToken.getAuthorities().contains(SecurityConstants.RECLAIM_AUTHORITY);
    }

    private Predicate<AccessTokenInfo> validAudiencePredicate(JWTAuthenticationToken jwtAuthenticationToken) {

        return accessTokenInfo -> authenticationConfig.getPasswordReset()
                .getAudience()
                .equals(jwtAuthenticationToken.getDetails().getAudience());
    }
}
