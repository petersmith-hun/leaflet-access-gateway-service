package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;

/**
 * Sets up the proper authentication error handling for OAuth login attempts. In case of a failed "login" for password
 * reset, the browser is redirected to the "access denied" page. Otherwise, the server simply responds with HTTP 401 Unauthorized response.
 *
 * @author Peter Smith
 */
public class OAuthAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        if (request.getRequestURI().equals(PATH_PASSWORD_RESET_CONFIRMATION)) {
            response.sendRedirect(SecurityConstants.PATH_ACCESS_DENIED);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
