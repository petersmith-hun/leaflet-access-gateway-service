package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.exception.ExpiredTokenException;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.web.model.AuthorizationError;
import hu.psprog.leaflet.lags.web.model.TokenRequestError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_ACCESS_DENIED;
import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_UNKNOWN_ERROR;

/**
 * Base controller implementation containing common REST API helpers.
 * Marked as a global controller advice so the defined exception handlers are registered for all REST endpoints.
 *
 * @author Peter Smith
 */
@RestControllerAdvice
@Controller
@Slf4j
public class BaseController {

    public static final String PATH_OAUTH_TOKEN = "/oauth/token";
    public static final String PATH_OAUTH_AUTHORIZE = "/oauth/authorize";
    public static final String PATH_OAUTH_INTROSPECT = "/oauth/introspect";

    private static final String VIEW_ERROR = "views/error";

    private static final String ATTRIBUTE_ERROR_CODE = "error_code";
    private static final String ATTRIBUTE_ERROR_DESCRIPTION = "error_description";
    private static final String ATTRIBUTE_REDIRECT_URI = "redirect_uri";
    private static final String DEFAULT_ERROR_DESCRIPTION = "Unknown error";

    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    @Autowired
    public BaseController(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    /**
     * GET|POST /access-denied
     *
     * Error controller entry point for authentication errors.
     * Renders the default error page with the error description and the ACCESS_DENIED OAuth error code.
     *
     * @param request {@link HttpServletRequest} object
     * @return populated {@link ModelAndView}
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = PATH_ACCESS_DENIED)
    public ModelAndView handleAuthenticationException(HttpServletRequest request) {

        Object exception = request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        String errorDescription = getErrorDescription(exception);

        return renderErrorPage(OAuthErrorCode.ACCESS_DENIED, errorDescription, request);
    }

    /**
     * GET|POST /unknown-error
     *
     * Error controller entry point for unknown errors.
     * Renders the default error page with the default error description and the SERVER_ERROR OAuth error code.
     *
     * @param request {@link HttpServletRequest} object
     * @return populated {@link ModelAndView}
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, path = PATH_UNKNOWN_ERROR)
    public ModelAndView handleError(HttpServletRequest request) {
        return renderErrorPage(OAuthErrorCode.SERVER_ERROR, DEFAULT_ERROR_DESCRIPTION, request);
    }

    /**
     * Exception handler for {@link OAuthTokenRequestException}s.
     * Logs the exception and wraps the message into a JSON response, along with the mapped status code of the OAuth error.
     *
     * @param exception {@link OAuthTokenRequestException} object containing the error message and the OAuth error code
     * @return response entity object containing the error message in {@link TokenRequestError} object
     */
    @ExceptionHandler(OAuthTokenRequestException.class)
    public ResponseEntity<TokenRequestError> handleTokenRequestException(OAuthTokenRequestException exception) {

        log.error(exception.getMessage(), exception);
        OAuthErrorCode errorCode = exception.getErrorCode();

        return ResponseEntity
                .status(errorCode.getMappedStatus())
                .body(new TokenRequestError(errorCode.getErrorCode(), exception.getMessage()));
    }

    /**
     * Exception handler for {@link OAuthAuthorizationException}s.
     * Renders the default error page with the exception message and the OAuth error code.
     *
     * @param exception {@link OAuthAuthorizationException} object
     * @param request {@link HttpServletRequest} object
     * @return populated {@link ModelAndView}
     */
    @ExceptionHandler(OAuthAuthorizationException.class)
    public ModelAndView handleAuthorizationException(OAuthAuthorizationException exception, HttpServletRequest request) {

        log.error(exception.getMessage(), exception);

        return renderErrorPage(exception.getErrorCode(), exception.getMessage(), request);
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
        return doHandleException(exception);
    }

    /**
     * Default exception handler for all unhandled exceptions.
     * Renders the default error page with the default error description and the SERVER_ERROR OAuth error code.
     *
     * @param exception {@link OAuthAuthorizationException} object
     * @param request {@link HttpServletRequest} object
     * @return populated {@link ModelAndView}
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception exception, HttpServletRequest request) {

        log.error(exception.getMessage(), exception);

        return renderErrorPage(OAuthErrorCode.SERVER_ERROR, DEFAULT_ERROR_DESCRIPTION, request);
    }

    private String getErrorDescription(Object exception) {

        return exception instanceof ExpiredTokenException
                ? "Your password reset token has expired, please request a new one"
                : "Access denied";
    }

    private ModelAndView renderErrorPage(OAuthErrorCode errorCode, String errorDescription, HttpServletRequest request) {

        return new ModelAndView(VIEW_ERROR)
                .addObject(ATTRIBUTE_ERROR_CODE, errorCode)
                .addObject(ATTRIBUTE_ERROR_DESCRIPTION, errorDescription)
                .addObject(ATTRIBUTE_REDIRECT_URI, getRedirectURIOnError(errorCode, request));
    }

    private String getRedirectURIOnError(OAuthErrorCode errorCode, HttpServletRequest request) {

        String redirectURI = null;
        if (errorCode != OAuthErrorCode.INVALID_GRANT) {
            redirectURI = request.getParameter(ATTRIBUTE_REDIRECT_URI);
        }

        if (Objects.isNull(redirectURI)) {
            redirectURI = oAuthConfigurationProperties.getDefaultRedirectOnError();
        }

        return redirectURI;
    }

    private ResponseEntity<AuthorizationError> doHandleException(Exception exception) {

        log.error(exception.getMessage(), exception);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AuthorizationError(exception.getMessage()));
    }
}
