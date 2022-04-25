package hu.psprog.leaflet.lags.core.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception to be thrown when an introspected token is already revoked.
 *
 * @author Peter Smith
 */
public class RevokedTokenException extends AuthenticationException {

    public RevokedTokenException(String msg) {
        super(msg);
    }
}
