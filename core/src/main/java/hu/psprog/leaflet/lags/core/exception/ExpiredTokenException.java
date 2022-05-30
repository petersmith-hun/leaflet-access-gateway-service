package hu.psprog.leaflet.lags.core.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception to be thrown when an expired token is received during password reset.
 *
 * @author Peter Smith
 */
public class ExpiredTokenException extends AuthenticationException {

    public ExpiredTokenException() {
        super("Token expired");
    }
}
