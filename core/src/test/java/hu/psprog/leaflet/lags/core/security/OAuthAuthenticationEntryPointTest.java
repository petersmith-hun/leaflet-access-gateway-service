package hu.psprog.leaflet.lags.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link OAuthAuthenticationEntryPoint}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OAuthAuthenticationEntryPoint entryPoint;

    @Test
    public void shouldCommenceForPasswordResetConfirmation() throws IOException {

        // given
        given(request.getRequestURI()).willReturn("/password-reset/confirmation");

        // when
        entryPoint.commence(request, response, null);

        // then
        verify(response).sendRedirect("/access-denied");
    }

    @Test
    public void shouldCommenceForEverythingElse() throws IOException {

        // given
        given(request.getRequestURI()).willReturn("/access-management/oauth-applications");

        // when
        entryPoint.commence(request, response, null);

        // then
        verify(response).sendError(401, "Unauthorized");
    }
}
