package hu.psprog.leaflet.lags.web.exception;

/**
 * Exception to be thrown when a non-local user attempts to edit their profile.
 *
 * @author Peter Smith
 */
public class NonLocalAccountEditAttemptException extends UserProfileAccessException {

    public NonLocalAccountEditAttemptException() {
        super("Non-local account edit attempt");
    }
}
