package hu.psprog.leaflet.lags.web.model;

/**
 * Authorization error message model class.
 * Contains the error message itself. Can be used in authorization exception error handlers.
 *
 * @author Peter Smith
 */
public record AuthorizationError(
        String message
) { }
