package hu.psprog.leaflet.lags.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Extension for the (default) {@link SavedRequestAwareAuthenticationSuccessHandler} to restore the authorization URL after
 * logging back in. This ensures that user can continue the authorization process after switching account.
 *
 * @author Peter Smith
 */
public class ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final String LOGOUT_REF_PARAMETER = "logoutRef";

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (Objects.nonNull(savedRequest)) {

            Optional<String> logoutRef = extractLogoutRef(savedRequest);

            if (logoutRef.isPresent()) {
                requestCache.removeRequest(request, response);
                getRedirectStrategy().sendRedirect(request, response, logoutRef.get());
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }

        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private Optional<String> extractLogoutRef(SavedRequest savedRequest) {

        return Optional.ofNullable(savedRequest.getParameterMap().get(LOGOUT_REF_PARAMETER))
                .filter(values -> values.length > 0)
                .map(values -> values[0])
                .map(Base64.getDecoder()::decode)
                .map(String::new);
    }
}
