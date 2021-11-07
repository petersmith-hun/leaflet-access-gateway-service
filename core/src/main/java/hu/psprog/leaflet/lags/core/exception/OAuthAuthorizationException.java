package hu.psprog.leaflet.lags.core.exception;

/**
 * Generic OAuth authorization exception.
 *
 * @author Peter Smith
 */
public class OAuthAuthorizationException extends RuntimeException {

    public OAuthAuthorizationException(String message) {
        super(message);
    }
}
