package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception to be thrown when importing an OAuth application registration is aborted due to an unexpected error.
 *
 * @author Peter Smith
 */
public class OAuthApplicationImportException extends RuntimeException {

    public OAuthApplicationImportException(String message) {
        super(message);
    }
}
