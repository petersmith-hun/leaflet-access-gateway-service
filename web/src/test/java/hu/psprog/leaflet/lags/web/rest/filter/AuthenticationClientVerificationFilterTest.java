package hu.psprog.leaflet.lags.web.rest.filter;

import hu.psprog.leaflet.lags.core.domain.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link AuthenticationClientVerificationFilter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationClientVerificationFilterTest {

    private static final String CLIENT_1_ID = "client-1";
    private static final String CLIENT_1_REDIRECT_URI = "https://dev.local:443/signup/callback";
    private static final String INVALID_REDIRECT_URI = "https://invalid.local:443/some/path";
    private static final OAuthClient VALID_CLIENT = new OAuthClient("client-name-1", ApplicationType.UI,
            CLIENT_1_ID, "secret-1", "aud-1", null, null,
            Arrays.asList(CLIENT_1_REDIRECT_URI, "https://dev.local:443/oauth/callback"));
    private static final String SIGNUP_PATH = "/signup";

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthenticationClientVerificationFilter authenticationClientVerificationFilter;

    @Test
    public void shouldVerifyRedirectionSuccessfully() throws ServletException, IOException {

        // given
        given(request.getParameter("client_id")).willReturn(CLIENT_1_ID);
        given(request.getParameter("redirect_uri")).willReturn(CLIENT_1_REDIRECT_URI);
        given(oAuthClientRegistry.getClientByClientID(CLIENT_1_ID)).willReturn(Optional.of(VALID_CLIENT));

        // when
        authenticationClientVerificationFilter.doFilterInternal(request, response, filterChain);

        // then
        // silent fallthrough expected
        verify(oAuthClientRegistry).getClientByClientID(CLIENT_1_ID);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldVerifyRedirectionThrowExceptionOnMissingClient() {

        // given
        given(request.getParameter("client_id")).willReturn(CLIENT_1_ID);
        given(request.getParameter("redirect_uri")).willReturn(CLIENT_1_REDIRECT_URI);
        given(oAuthClientRegistry.getClientByClientID(CLIENT_1_ID)).willReturn(Optional.empty());

        // when
        assertThrows(AuthenticationException.class, () -> authenticationClientVerificationFilter.doFilterInternal(request, response, filterChain));

        // then
        // exception expected
        verify(oAuthClientRegistry).getClientByClientID(CLIENT_1_ID);
        verifyNoInteractions(filterChain);
    }

    @Test
    public void shouldVerifyRedirectionThrowExceptionOnNonRegisteredRedirectURI() {

        // given
        given(request.getParameter("client_id")).willReturn(CLIENT_1_ID);
        given(request.getParameter("redirect_uri")).willReturn(INVALID_REDIRECT_URI);
        given(oAuthClientRegistry.getClientByClientID(CLIENT_1_ID)).willReturn(Optional.of(VALID_CLIENT));

        // when
        assertThrows(AuthenticationException.class, () -> authenticationClientVerificationFilter.doFilterInternal(request, response, filterChain));

        // then
        // exception expected
        verify(oAuthClientRegistry).getClientByClientID(CLIENT_1_ID);
        verifyNoInteractions(filterChain);
    }

    @ParameterizedTest
    @ValueSource(strings = {SIGNUP_PATH, "/login", "/authorize", "/token"})
    public void shouldFilterActivateOnlyOnSignUpPath(String currentServletPath) {

        // given
        boolean expectedResult = !SIGNUP_PATH.equals(currentServletPath);

        given(request.getServletPath()).willReturn(currentServletPath);

        // when
        boolean result = authenticationClientVerificationFilter.shouldNotFilter(request);

        // then
        assertThat(result, is(expectedResult));
    }
}
