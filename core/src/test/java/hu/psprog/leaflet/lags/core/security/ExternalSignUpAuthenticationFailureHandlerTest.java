package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ExternalSignUpAuthenticationFailureHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ExternalSignUpAuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private ExternalSignUpAuthenticationFailureHandler externalSignUpAuthenticationFailureHandler;

    @Test
    public void shouldOnAuthenticationFailureSendRedirectionForExternalAuthenticationException() throws IOException {

        // given
        AuthenticationException exception = new ExternalAuthenticationException(SignUpStatus.ADDRESS_IN_USE, "Failure");

        // when
        externalSignUpAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(response).sendRedirect("/login?ext_auth=ADDRESS_IN_USE");
    }

    @Test
    public void shouldOnAuthenticationFailureSendRedirectionForAnyOtherAuthenticationException() throws IOException {

        // given
        AuthenticationException exception = new OAuth2AuthenticationException("invalid_scope");

        // when
        externalSignUpAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(response).sendRedirect("/login?ext_auth=FAILURE");
    }
}
