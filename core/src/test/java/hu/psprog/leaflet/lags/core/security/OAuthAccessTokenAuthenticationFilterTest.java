package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.exception.ExpiredTokenException;
import hu.psprog.leaflet.lags.core.exception.JWTTokenParsingException;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link OAuthAccessTokenAuthenticationFilter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthAccessTokenAuthenticationFilterTest {

    private static final String JWT_TOKEN = "jwt-token-1";
    private static final String BEARER_TOKEN = String.format("Bearer %s", JWT_TOKEN);
    private static final TokenClaims TOKEN_CLAIMS = TokenClaims.builder()
            .scope("read:all write:all")
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

    private OAuthAccessTokenAuthenticationFilter oAuthAccessTokenAuthenticationFilter;

    @Test
    public void shouldAttemptAuthenticationSuccessfullyParseTokenFromQueryString() {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(servletRequest -> servletRequest.getParameter("token"));
        oAuthAccessTokenAuthenticationFilter.setAuthenticationManager(authenticationManager);

        given(request.getParameter("token")).willReturn(JWT_TOKEN);
        given(tokenHandler.parseToken(JWT_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(authenticationManager.authenticate(any(JWTAuthenticationToken.class))).will(invocation -> invocation.getArgument(0));

        // when
        Authentication result = oAuthAccessTokenAuthenticationFilter.attemptAuthentication(request, response);

        // then
        assertThat(result instanceof JWTAuthenticationToken, is(true));

        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) result;
        assertThat(jwtAuthenticationToken.getPrincipal(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(jwtAuthenticationToken.getCredentials(), equalTo(JWT_TOKEN));
        assertThat(jwtAuthenticationToken.getDetails(), equalTo(TOKEN_CLAIMS));
        assertThat(jwtAuthenticationToken.getAuthorities(), equalTo(AuthorityUtils.createAuthorityList(TOKEN_CLAIMS.getScopeAsArray())));
        assertThat(jwtAuthenticationToken.isAuthenticated(), is(false));
    }

    @Test
    public void shouldAttemptAuthenticationSuccessfullyParseTokenFromHeader() {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(servletRequest -> servletRequest.getHeader("Authorization"));
        oAuthAccessTokenAuthenticationFilter.setAuthenticationManager(authenticationManager);

        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        given(tokenHandler.parseToken(JWT_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(authenticationManager.authenticate(any(JWTAuthenticationToken.class))).will(invocation -> invocation.getArgument(0));

        // when
        Authentication result = oAuthAccessTokenAuthenticationFilter.attemptAuthentication(request, response);

        // then
        assertThat(result instanceof JWTAuthenticationToken, is(true));

        JWTAuthenticationToken jwtAuthenticationToken = (JWTAuthenticationToken) result;
        assertThat(jwtAuthenticationToken.getPrincipal(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(jwtAuthenticationToken.getCredentials(), equalTo(JWT_TOKEN));
        assertThat(jwtAuthenticationToken.getDetails(), equalTo(TOKEN_CLAIMS));
        assertThat(jwtAuthenticationToken.getAuthorities(), equalTo(AuthorityUtils.createAuthorityList(TOKEN_CLAIMS.getScopeAsArray())));
        assertThat(jwtAuthenticationToken.isAuthenticated(), is(false));
    }

    @Test
    public void shouldAttemptAuthenticationThrowExceptionOnMissionToken() {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(servletRequest -> servletRequest.getHeader("Authorization"));
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        doThrow(JWTTokenParsingException.class).when(tokenHandler).parseToken(JWT_TOKEN);

        // when
        assertThrows(ExpiredTokenException.class, () -> oAuthAccessTokenAuthenticationFilter.attemptAuthentication(request, response));

        // then
        // exception expected
    }

    @Test
    public void shouldAttemptAuthenticationThrowExceptionOnExpiredToken() {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(servletRequest -> servletRequest.getParameter("token"));
        given(request.getParameter("token")).willReturn(null);

        // when
        assertThrows(InsufficientAuthenticationException.class, () -> oAuthAccessTokenAuthenticationFilter.attemptAuthentication(request, response));

        // then
        // exception expected
    }

    @Test
    public void shouldAttemptAuthenticationThrowBadCredentialsExceptionOnAnyOtherUnexpectedException() {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(servletRequest -> servletRequest.getHeader("Authorization"));
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        doThrow(RuntimeException.class).when(tokenHandler).parseToken(JWT_TOKEN);

        // when
        assertThrows(BadCredentialsException.class, () -> oAuthAccessTokenAuthenticationFilter.attemptAuthentication(request, response));

        // then
        // exception expected
    }

    @Test
    public void shouldSuccessfulAuthenticationPreventCallingSuccessHandlers() throws ServletException, IOException {

        // given
        prepareOAuthAccessTokenAuthenticationFilter(null);
        oAuthAccessTokenAuthenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);

        // when
        oAuthAccessTokenAuthenticationFilter.successfulAuthentication(request, response, filterChain, authentication);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication(), equalTo(authentication));

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(authenticationSuccessHandler);
        verifyNoMoreInteractions(filterChain, request, response, authentication);
    }

    private void prepareOAuthAccessTokenAuthenticationFilter(Function<HttpServletRequest, String> tokenExtractionFunction) {
        oAuthAccessTokenAuthenticationFilter = new OAuthAccessTokenAuthenticationFilter(tokenHandler, "/**", tokenExtractionFunction);
    }
}
