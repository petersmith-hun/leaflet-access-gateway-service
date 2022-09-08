package hu.psprog.leaflet.lags.core.exception;

import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception to be thrown where an error occurs during the sign-up process of an external user account.
 *
 * @author Peter Smith
 */
public class ExternalAuthenticationException extends AuthenticationException {

    private final SignUpStatus signUpStatus;

    public ExternalAuthenticationException(SignUpStatus signUpStatus, String message) {
        super(message);
        this.signUpStatus = signUpStatus;
    }

    /**
     * Returns the assigned sign-up status.
     *
     * @return sign-up status as an item of the {@link SignUpStatus} enum
     */
    public SignUpStatus getSignUpStatus() {
        return signUpStatus;
    }
}
