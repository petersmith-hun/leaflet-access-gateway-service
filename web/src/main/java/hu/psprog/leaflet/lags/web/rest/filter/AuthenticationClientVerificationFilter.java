package hu.psprog.leaflet.lags.web.rest.filter;

import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_SIGNUP;

/**
 * Filter implementation to verify if the authentication related request (such as sign-up) was started by a registered OAuth client.
 * An authentication request like this must contain a registered client ID (client_id) and the requested redirection URI (redirect_uri)
 * parameters as query parameters. An unknown client ID or a non-registered redirect URI will trigger an {@link AuthenticationException}.
 *
 * The filter activates only on the following endpoints:
 *  - /signup
 *
 * @author Peter Smith
 */
@Component
public class AuthenticationClientVerificationFilter extends OncePerRequestFilter {

    private final OAuthClientRegistry oAuthClientRegistry;

    @Autowired
    public AuthenticationClientVerificationFilter(OAuthClientRegistry oAuthClientRegistry) {
        this.oAuthClientRegistry = oAuthClientRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        verifyRedirection(request);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PATH_SIGNUP.equals(request.getServletPath());
    }

    private void verifyRedirection(HttpServletRequest request) {

        String clientID = request.getParameter(OAuthConstants.Request.CLIENT_ID);
        String redirectURI = request.getParameter(OAuthConstants.Request.REDIRECT_URI);
        boolean validRedirection = isValidRedirection(clientID, redirectURI);

        if (!validRedirection) {
            throw new AuthenticationException("Invalid redirect URI.");
        }
    }

    private boolean isValidRedirection(String clientID, String redirectURI) {

        return oAuthClientRegistry.getClientByClientID(clientID)
                .filter(oAuthClient -> oAuthClient.getAllowedCallbacks().contains(redirectURI))
                .isPresent();
    }
}
