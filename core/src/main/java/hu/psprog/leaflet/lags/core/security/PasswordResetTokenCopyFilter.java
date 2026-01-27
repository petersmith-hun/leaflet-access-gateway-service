package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * "Copies" over the password reset access token to the header, by wrapping the getHeader() call and piping the access
 * token from the "token" query parameter into its return. Only applies to the "/password-reset/confirmation" endpoints.
 *
 * @author Peter Smith
 */
public class PasswordResetTokenCopyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        if (SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION.equals(request.getRequestURI())) {

            filterChain.doFilter(new PasswordResetTokenCopyRequestWrapper(request), response);

        } else {
            filterChain.doFilter(request, response);
        }
    }

    private static class PasswordResetTokenCopyRequestWrapper extends HttpServletRequestWrapper {

        private static final String HEADER_AUTHORIZATION = "Authorization";
        private static final String BEARER_TEMPLATE = "Bearer %s";

        public PasswordResetTokenCopyRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {

            return HEADER_AUTHORIZATION.equals(name)
                    ? BEARER_TEMPLATE.formatted(super.getRequest().getParameter(SecurityConstants.QUERY_PARAMETER_TOKEN))
                    : super.getHeader(name);
        }
    }
}
