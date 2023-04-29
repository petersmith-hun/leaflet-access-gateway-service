package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception to be thrown when parsing the given JWT token fails.
 *
 * @author Peter Smith
 */
public class JWTTokenParsingException extends RuntimeException {

    public JWTTokenParsingException(Throwable cause) {
        super(cause);
    }
}
