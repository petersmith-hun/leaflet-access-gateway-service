package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.TokenClaims;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link PasswordResetAuthenticationFilter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetAuthenticationFilterTest {

    private static final String JWT_TOKEN = "jwt-token-1";
    private static final TokenClaims TOKEN_CLAIMS = TokenClaims.builder()
            .scopes(new String[] {"read:all", "write:all"})
            .username("user1")
            .build();

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private FilterChain filterChain;

    @Mock
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @InjectMocks
    private PasswordResetAuthenticationFilter passwordResetAuthenticationFilter;

    @Test
    public void shouldAttemptAuthenticationSuccessfullyParseToken() {

        // given
        passwordResetAuthenticationFilter.setAuthenticationManager(authenticationManager);

        given(request.getParameter("token")).willReturn(JWT_TOKEN);
        given(tokenHandler.parseToken(JWT_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(authenticationManager.authenticate(any(JWTAuthenticationToken.class))).will(invocation -> invocation.getArgument(0));

        // when
        Authentication result = passwordResetAuthenticationFilter.attemptAuthentication(request, response);

        // then
        assertThat(result instanceof JWTAuthenticationToken, is(true));

        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) result;
        assertThat(jwtAuthenticationToken.getPrincipal(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(jwtAuthenticationToken.getCredentials(), equalTo(JWT_TOKEN));
        assertThat(jwtAuthenticationToken.getDetails(), equalTo(TOKEN_CLAIMS));
        assertThat(jwtAuthenticationToken.getAuthorities(), equalTo(AuthorityUtils.createAuthorityList(TOKEN_CLAIMS.getScopes())));
        assertThat(jwtAuthenticationToken.isAuthenticated(), is(false));
    }

    @Test
    public void shouldAttemptAuthenticationThrowExceptionOnMissionToken() {

        // given
        given(request.getParameter("token")).willReturn(null);

        // when
        assertThrows(InsufficientAuthenticationException.class, () -> passwordResetAuthenticationFilter.attemptAuthentication(request, response));

        // then
        // exception expected
    }

    @Test
    public void shouldSuccessfulAuthenticationPreventCallingSuccessHandlers() throws ServletException, IOException {

        // given
        passwordResetAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        // when
        passwordResetAuthenticationFilter.successfulAuthentication(request, response, filterChain, authentication);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication(), equalTo(authentication));

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationSuccessHandler);
        verifyNoMoreInteractions(filterChain, request, response, authentication);
    }
}
