package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.web.model.AuthorizationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
        assertThat(result.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(result.getBody(), equalTo(expectedAuthorizationError));
    }
}
