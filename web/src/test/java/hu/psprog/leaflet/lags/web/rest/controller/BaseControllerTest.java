package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.exception.RevokedTokenException;
import hu.psprog.leaflet.lags.web.model.AuthorizationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link BaseController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @InjectMocks
    private BaseController baseController;

    @Test
    public void shouldHandleAuthorizationException() {

        // given
        String exceptionMessage = "OAuth authorization failed";
        OAuthAuthorizationException exception = new OAuthAuthorizationException(exceptionMessage);
        AuthorizationError expectedAuthorizationError = new AuthorizationError(exceptionMessage);

        // when
        ResponseEntity<AuthorizationError> result = baseController.handleAuthorizationException(exception);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.FORBIDDEN));
        assertThat(result.getBody(), equalTo(expectedAuthorizationError));
    }

    @ParameterizedTest
    @ValueSource(classes = {
            AuthenticationException.class,
            RevokedTokenException.class
    })
    public void shouldHandleAuthenticationException(Class<? extends Exception> exceptionClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // given
        String exceptionMessage = "Authentication failed";
        Exception exception = exceptionClass.getConstructor(String.class).newInstance(exceptionMessage);
        AuthorizationError expectedAuthorizationError = new AuthorizationError(exceptionMessage);

        // when
        ResponseEntity<AuthorizationError> result = baseController.handleAuthenticationException(exception);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(result.getBody(), equalTo(expectedAuthorizationError));
    }
}
