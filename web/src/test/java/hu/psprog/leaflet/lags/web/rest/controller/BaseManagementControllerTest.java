package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.api.rest.response.common.ErrorMessageDataModel;
import hu.psprog.leaflet.api.rest.response.common.ValidationErrorMessageDataModel;
import hu.psprog.leaflet.api.rest.response.common.ValidationErrorMessageListDataModel;
import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link BaseManagementController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class BaseManagementControllerTest {

    @Mock
    private MethodArgumentNotValidException validationException;

    @InjectMocks
    private RoleManagementController controller;

    @Test
    public void shouldHandleValidationError() {

        // given
        var expectedResponse = new ValidationErrorMessageListDataModel(List.of(
                validationError("field1", "not null"),
                validationError("field2", "not empty"),
                validationError("object1", "very bad"),
                validationError("object2", "slightly less bad")
        ));

        given(validationException.getFieldErrors()).willReturn(List.of(
                fieldError("field1", "not null"),
                fieldError("field2", "not empty")
        ));

        given(validationException.getGlobalErrors()).willReturn(List.of(
                objectError("object1", "very bad"),
                objectError("object2", "slightly less bad")
        ));

        // when
        var result = controller.handleValidationError(validationException);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @ParameterizedTest
    @MethodSource("exceptionHandlerDataProvider")
    public void shouldHandleExceptions(Exception exception, HttpStatus expectedStatus) {

        // given
        var expectedResponse = ErrorMessageDataModel.getBuilder()
                .withMessage(exception.getMessage())
                .build();

        // when
        var result = controller.handleException(exception);

        // then
        assertThat(result.getStatusCode(), equalTo(expectedStatus));
        assertThat(result.getBody(), equalTo(expectedResponse));
    }

    private ValidationErrorMessageDataModel validationError(String field, String message) {

        return ValidationErrorMessageDataModel.getBuilder()
                .withField(field)
                .withMessage(message)
                .build();
    }

    private FieldError fieldError(String field, String message) {
        return new FieldError("ignored", field, message);
    }

    private ObjectError objectError(String objectName, String message) {
        return new ObjectError(objectName, message);
    }

    private static Stream<Arguments> exceptionHandlerDataProvider() {

        return Stream.of(
                Arguments.of(ResourceNotFoundException.role(UUID.randomUUID()), HttpStatus.NOT_FOUND),
                Arguments.of(new AuthorizationDeniedException("Forbidden"), HttpStatus.FORBIDDEN),
                Arguments.of(ConflictingResourceException.onCreate(ManagedResourceType.ROLE), HttpStatus.CONFLICT),
                Arguments.of(new IllegalArgumentException("Bad request"), HttpStatus.BAD_REQUEST),
                Arguments.of(new Exception("Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }
}
