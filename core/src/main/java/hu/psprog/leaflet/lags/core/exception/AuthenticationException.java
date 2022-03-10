package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception class for authentication related exceptions.
 *
 * @author Peter Smith
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
