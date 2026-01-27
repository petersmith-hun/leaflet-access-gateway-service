package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

/**
 * {@link AuthenticationFailureHandler} implementation used to indicate an error in the sign-in process of an
 * external user. The implementation simply redirects the user back to the main login page and passes a status value
 * in query parameter, so the UI can show the proper error message.
 *
 * @author Peter Smith
 */
public class ExternalSignUpAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String REDIRECT_PATH_TEMPLATE = "/login?ext_auth=%s";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        SignUpStatus signUpStatus = exception instanceof ExternalAuthenticationException
                ? ((ExternalAuthenticationException) exception).getSignUpStatus()
                : SignUpStatus.FAILURE;

        response.sendRedirect(String.format(REDIRECT_PATH_TEMPLATE, signUpStatus.name()));
    }
}
