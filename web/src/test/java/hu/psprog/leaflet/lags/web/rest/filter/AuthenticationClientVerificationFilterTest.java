package hu.psprog.leaflet.lags.web.rest.filter;

import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
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

    private static final String SIGNUP_PATH = "/signup";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private SavedRequest savedRequest;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthenticationClientVerificationFilter authenticationClientVerificationFilter;

    @Test
    public void shouldVerifySavedRequestPresenceSuccessfully() throws ServletException, IOException {

        // given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute("SPRING_SECURITY_SAVED_REQUEST")).willReturn(savedRequest);

        // when
        authenticationClientVerificationFilter.doFilterInternal(request, response, filterChain);

        // then
        // silent fallthrough expected
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldVerifySavedRequestPresenceThrowExceptionOnMissingSavedRequest() {

        // given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute("SPRING_SECURITY_SAVED_REQUEST")).willReturn(null);

        // when
        Throwable result = assertThrows(AuthenticationException.class, () -> authenticationClientVerificationFilter.doFilterInternal(request, response, filterChain));

        // then
        // exception expected
        verifyNoInteractions(filterChain);
        assertThat(result.getMessage(), equalTo("Sign up was not started via OAuth authorization request."));
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
