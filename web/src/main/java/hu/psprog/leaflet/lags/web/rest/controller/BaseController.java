package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.web.model.AuthorizationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Base controller implementation containing common REST API helpers.
 * Marked as a global controller advice so the defined exception handlers are registered for all REST endpoints.
 *
 * @author Peter Smith
 */
@RestControllerAdvice
@Slf4j
public class BaseController {

    public static final String PATH_LOGIN = "/login";
    public static final String PATH_SIGNUP = "/signup";
    public static final String PATH_OAUTH_TOKEN = "/oauth/token";
    public static final String PATH_OAUTH_AUTHORIZE = "/oauth/authorize";

    /**
     * Exception handler for {@link OAuthAuthorizationException}s.
     * Logs the exception and wraps the message into a JSON response, along with an HTTP 403 Forbidden status code.
     *
     * @param exception {@link OAuthAuthorizationException} object
     * @return response entity object containing the error message in {@link AuthorizationError} object
     */
    @ExceptionHandler(OAuthAuthorizationException.class)
    public ResponseEntity<AuthorizationError> handleAuthorizationException(OAuthAuthorizationException exception) {

        log.error(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new AuthorizationError(exception.getMessage()));
    }
}
