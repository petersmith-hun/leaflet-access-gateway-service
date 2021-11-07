package hu.psprog.leaflet.lags.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Authorization error message model class.
 * Contains the error message itself. Can be used in authorization exception error handlers.
 *
 * @author Peter Smith
 */
@Data
@AllArgsConstructor
public class AuthorizationError {

    private final String message;
}
