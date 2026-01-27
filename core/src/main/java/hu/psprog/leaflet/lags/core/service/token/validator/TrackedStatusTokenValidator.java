package hu.psprog.leaflet.lags.core.service.token.validator;

import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * {@link OAuth2TokenValidator} implementation to check if the tokens is not yet revoked.
 *
 * @author Peter Smith
 */
public class TrackedStatusTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenTracker tokenTracker;

    public TrackedStatusTokenValidator(TokenTracker tokenTracker) {
        this.tokenTracker = tokenTracker;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {

        return tokenTracker.retrieveTokenInfo(token.getId())
                .filter(accessTokenInfo -> accessTokenInfo.getStatus() == TokenStatus.ACTIVE)
                .map(accessTokenInfo -> OAuth2TokenValidatorResult.success())
                .orElseGet(() -> OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN)));
    }
}
