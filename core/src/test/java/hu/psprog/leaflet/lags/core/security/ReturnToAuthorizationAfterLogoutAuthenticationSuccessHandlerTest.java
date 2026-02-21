package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandlerTest {

    private static final String LOGOUT_REF = "https://dev.local:9999/authorize?client_id=client-1";
    private static final String B64_ENCODED_LOGOUT_REF = prepareBase64EncodedLogoutRef();

    private final HttpServletRequest request = new MockHttpServletRequest();
    private final HttpServletResponse response = new MockHttpServletResponse();

    @Mock
    private Authentication authentication;

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private ReturnToAuthorizationAfterLogoutAuthenticationSuccessHandler returnToAuthorizationAfterLogoutAuthenticationSuccessHandler;

    @Test
    public void shouldOnAuthenticationSuccessSendRedirectionToLogoutRef() throws ServletException, IOException {

        // given
        RequestCache requestCache = new HttpSessionRequestCache();
        ((MockHttpServletRequest) request).setParameter("logoutRef", B64_ENCODED_LOGOUT_REF);
        requestCache.saveRequest(request, response);

        // when
        returnToAuthorizationAfterLogoutAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getStatus(), equalTo(302));
        assertThat(response.getHeader("Location"), equalTo(LOGOUT_REF));
        assertThat(requestCache.getRequest(request, response), nullValue());

        verify(userManagementService).updateLastLogin(authentication);
    }

    @Test
    public void shouldOnAuthenticationSuccessSendDefaultRedirectionOnMissingLogoutRef() throws ServletException, IOException {

        // given
        RequestCache requestCache = new HttpSessionRequestCache();
        ((MockHttpServletRequest) request).setScheme("https");
        ((MockHttpServletRequest) request).setServerName("dev.local");
        ((MockHttpServletRequest) request).setServerPort(9999);
        requestCache.saveRequest(request, response);

        // when
        returnToAuthorizationAfterLogoutAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getStatus(), equalTo(302));
        assertThat(response.getHeader("Location"), equalTo("https://dev.local:9999?continue"));

        verify(userManagementService).updateLastLogin(authentication);
    }

    @Test
    public void shouldOnAuthenticationSuccessSendDefaultRedirectionOnMissingSavedRequest() throws ServletException, IOException {

        // when
        returnToAuthorizationAfterLogoutAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertThat(response.getStatus(), equalTo(302));
        assertThat(response.getHeader("Location"), equalTo("/"));

        verify(userManagementService).updateLastLogin(authentication);
    }

    private static String prepareBase64EncodedLogoutRef() {
        return Base64.getEncoder().encodeToString(LOGOUT_REF.getBytes());
    }
}
