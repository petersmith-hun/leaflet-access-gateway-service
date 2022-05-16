package hu.psprog.leaflet.lags.web.rest.filter;

import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_SIGNUP;

/**
 * Filter implementation to verify if the authentication related request (such as sign-up) was started by an authorization request.
 * This means the currently processed request should contain a saved request (caused by the redirection from the /authorize endpoint).
 *
 * In case the request does not contain a saved request, it will trigger an {@link AuthenticationException}.
 *
 * The filter activates only on the following endpoints:
 *  - /signup
 *
 * @author Peter Smith
 */
@Component
public class AuthenticationClientVerificationFilter extends OncePerRequestFilter {

    private final RequestCache requestCache;

    @Autowired
    public AuthenticationClientVerificationFilter() {
        this.requestCache = new HttpSessionRequestCache();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        verifySavedRequestPresence(request, response);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PATH_SIGNUP.equals(request.getServletPath());
    }

    private void verifySavedRequestPresence(HttpServletRequest request, HttpServletResponse response) {

        if (Objects.isNull(requestCache.getRequest(request, response))) {
            throw new AuthenticationException("Sign up was not started via OAuth authorization request.");
        }
    }
}
