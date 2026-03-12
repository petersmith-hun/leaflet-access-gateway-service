package hu.psprog.leaflet.lags.web.exception;

/**
 * Exception to be thrown when the return directive is missing from a profile management access request.
 *
 * @author Peter Smith
 */
public class MissingReturnDirectiveException extends UserProfileAccessException {

    public MissingReturnDirectiveException() {
        super("A registered return directive must be present");
    }
}
