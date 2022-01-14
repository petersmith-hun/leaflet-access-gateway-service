package hu.psprog.leaflet.lags.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extension of the (default) {@link SimpleUrlLogoutSuccessHandler} to save the current request object before logging the user out.
 * This is required to store the authorization URL with the populated parameters, so the user can switch account without
 * unintentionally interrupting the authorization process.
 *
 * @author Peter Smith
 */
public class RequestSavingLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        requestCache.saveRequest(request, response);
        super.onLogoutSuccess(request, response, authentication);
    }
}
