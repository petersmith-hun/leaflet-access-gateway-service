package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
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

    public static final String PATH_OAUTH_TOKEN = "/oauth/token";
    public static final String PATH_OAUTH_AUTHORIZE = "/oauth/authorize";
    public static final String PATH_OAUTH_INTROSPECT = "/oauth/introspect";

    /**
     * Exception handler for {@link OAuthAuthorizationException}s.
     * Logs the exception and wraps the message into a JSON response, along with an HTTP 403 Forbidden status code.
     *
     * @param exception {@link OAuthAuthorizationException} object
     * @return response entity object containing the error message in {@link AuthorizationError} object
     */
    @ExceptionHandler(OAuthAuthorizationException.class)
    public ResponseEntity<AuthorizationError> handleAuthorizationException(OAuthAuthorizationException exception) {
        return handleException(exception, HttpStatus.FORBIDDEN);
    }

    /**
     * Exception handler for {@link AuthenticationException}s and {@link org.springframework.security.core.AuthenticationException}s.
     * Logs the exception and wraps the message into a JSON response, along with an HTTP 401 Unauthorized status code.
     *
     * @param exception {@link Exception} object
     * @return response entity object containing the error message in {@link AuthorizationError} object
     */
    @ExceptionHandler({AuthenticationException.class, org.springframework.security.core.AuthenticationException.class})
    public ResponseEntity<AuthorizationError> handleAuthenticationException(Exception exception) {
        return handleException(exception, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<AuthorizationError> handleException(Exception exception, HttpStatus httpStatus) {

        log.error(exception.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(new AuthorizationError(exception.getMessage()));
    }
}
