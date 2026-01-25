package hu.psprog.leaflet.lags.core.exception;

import java.util.UUID;

/**
 * Exception to be thrown when the requested permission is not found.
 *
 * @author Peter Smith
 */
public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID id) {
        super("Permission by ID=%s not found: ".formatted(id));
    }
}
