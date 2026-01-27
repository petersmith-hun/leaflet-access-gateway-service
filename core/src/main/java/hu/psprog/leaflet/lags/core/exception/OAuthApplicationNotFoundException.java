package hu.psprog.leaflet.lags.core.exception;

import java.util.UUID;

/**
 * Exception to be thrown when the requested application definition is not found.
 *
 * @author Peter Smith
 */
public class OAuthApplicationNotFoundException extends RuntimeException {
    public OAuthApplicationNotFoundException(UUID applicationID) {
        super("OAuth application by ID=%s not found".formatted(applicationID));
    }
}
