package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.api.rest.response.common.ErrorMessageDataModel;
import hu.psprog.leaflet.api.rest.response.common.ValidationErrorMessageDataModel;
import hu.psprog.leaflet.api.rest.response.common.ValidationErrorMessageListDataModel;
import hu.psprog.leaflet.lags.core.exception.ConflictingOAuthApplicationRegistrationException;
import hu.psprog.leaflet.lags.core.exception.ConflictingPermissionException;
import hu.psprog.leaflet.lags.core.exception.OAuthApplicationNotFoundException;
import hu.psprog.leaflet.lags.core.exception.PermissionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Stream;

/**
 * Common utilities for management controllers.
 *
 * @author Peter Smith
 */
@Slf4j
abstract class BaseManagementController {

    /**
     * Exception handler for validation errors. Sets the response status to HTTP 400 Bad Request, and responds with the
     * validation errors as {@link ValidationErrorMessageListDataModel}.
     *
     * @param exception validation exception
     * @return validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorMessageListDataModel handleValidationError(MethodArgumentNotValidException exception) {

        log.error("Failed to validate management request", exception);

        Stream<ValidationErrorMessageDataModel> fieldErrors = exception.getFieldErrors()
                .stream()
                .map(fieldError -> ValidationErrorMessageDataModel.getBuilder()
                        .withField(fieldError.getField())
                        .withMessage(fieldError.getDefaultMessage())
                        .build());

        Stream<ValidationErrorMessageDataModel> globalErrors = exception.getGlobalErrors()
                .stream()
                .map(globalError -> ValidationErrorMessageDataModel.getBuilder()
                        .withField(globalError.getObjectName())
                        .withMessage(globalError.getDefaultMessage())
                        .build());

        return ValidationErrorMessageListDataModel.getBuilder()
                .withValidation(Stream.concat(globalErrors, fieldErrors).toList())
                .build();
    }

    /**
     * Common exception handler for management controllers. Based on the exception, responds with a corresponding HTTP Status.
     *
     * @param exception exception to be handled
     * @return response entity generated for the exception
     */
    @ExceptionHandler
    public ResponseEntity<ErrorMessageDataModel> handleException(Exception exception) {

        log.error("Failed to process management operation: {}", exception.getMessage(), exception);

        return ResponseEntity
                .status(switch (exception) {
                    case OAuthApplicationNotFoundException ignored -> HttpStatus.NOT_FOUND;
                    case PermissionNotFoundException ignored -> HttpStatus.NOT_FOUND;
                    case AuthorizationDeniedException ignored -> HttpStatus.FORBIDDEN;
                    case ConflictingOAuthApplicationRegistrationException ignored -> HttpStatus.CONFLICT;
                    case ConflictingPermissionException ignored -> HttpStatus.CONFLICT;
                    case IllegalArgumentException ignored -> HttpStatus.BAD_REQUEST;
                    default -> HttpStatus.INTERNAL_SERVER_ERROR;
                })
                .body(ErrorMessageDataModel.getBuilder()
                        .withMessage(exception.getMessage())
                        .build());
    }
}
