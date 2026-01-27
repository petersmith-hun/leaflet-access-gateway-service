package hu.psprog.leaflet.lags.core.service.token.validator;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * {@link OAuth2TokenValidator} implementation to check if the given password reset token has the necessary limitations,
 * namely, being single-scoped (write:reclaim permission only) and the audience is pointing to LAGS itself. These
 * validations are ignored for any other kind of access tokens.
 *
 * @author Peter Smith
 */
public class PasswordResetTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final AuthenticationConfig authenticationConfig;

    public PasswordResetTokenValidator(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {

        if (!isPasswordResetRequest(token)) {
            return OAuth2TokenValidatorResult.success();
        }

        return isReclaimOnlyToken(token) && isValidAudience(token)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN));
    }

    private boolean isReclaimOnlyToken(Jwt token) {

        return token.getClaim(OAuthConstants.Token.SCOPE)
                .equals(SecurityConstants.RECLAIM_AUTHORITY.getAuthority());
    }

    private boolean isValidAudience(Jwt token) {

        return token.getClaimAsStringList(OAuthConstants.Token.AUDIENCE)
                .contains(authenticationConfig.getPasswordReset().getAudience());
    }

    private boolean isPasswordResetRequest(Jwt token) {
        return token.getSubject().startsWith("password-reset|");
    }
}
