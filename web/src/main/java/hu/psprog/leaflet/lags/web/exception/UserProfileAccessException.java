package hu.psprog.leaflet.lags.web.exception;

/**
 * Common runtime exception backing specific user profile operation exceptions.
 *
 * @author Peter Smith
 */
public class UserProfileAccessException extends RuntimeException {

    public UserProfileAccessException(String message) {
        super(message);
    }
}
