package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.exception.RevokedTokenException;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
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
        Optional<AccessTokenInfo> token = tokenTracker.retrieveTokenInfo(jwtAuthenticationToken.getDetails().getTokenID());
        Object principal = authentication.getPrincipal();

        token.filter(activeTokenPredicate())
                .orElseThrow(() -> new RevokedTokenException(String.format("Token for user [%s] has already been revoked", principal)));

        if (isPasswordResetRequest(jwtAuthenticationToken)) {
            token.filter(singleAuthorityPredicate(jwtAuthenticationToken))
                    .filter(validAudiencePredicate(jwtAuthenticationToken))
                    .orElseThrow(() -> new RevokedTokenException(String.format("Password reset token for user [%s] is invalid", principal)));
        }

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

    private Predicate<AccessTokenInfo> singleAuthorityPredicate(JWTAuthenticationToken jwtAuthenticationToken) {
        return accessTokenInfo -> jwtAuthenticationToken.getAuthorities().size() == 1;
    }

    private Predicate<AccessTokenInfo> validAudiencePredicate(JWTAuthenticationToken jwtAuthenticationToken) {

        return accessTokenInfo -> authenticationConfig.getPasswordReset()
                .getAudience()
                .equals(jwtAuthenticationToken.getDetails().getAudience());
    }

    private boolean isPasswordResetRequest(JWTAuthenticationToken authentication) {
        return authentication.getAuthorities().contains(SecurityConstants.RECLAIM_AUTHORITY);
    }
}
