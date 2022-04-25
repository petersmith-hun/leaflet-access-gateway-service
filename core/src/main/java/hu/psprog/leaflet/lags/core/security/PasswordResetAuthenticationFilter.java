package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.TokenClaims;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.QUERY_PARAMETER_TOKEN;

/**
 * {@link AbstractAuthenticationProcessingFilter} implementation to verify and authenticate password reset requests.
 *
 * Activates only on the /password-reset/confirmation endpoints. For the successful authentication, the filter requires
 * the access token to be passed as a query parameter named "token".
 *
 * @author Peter Smith
 */
public class PasswordResetAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final TokenHandler tokenHandler;

    public PasswordResetAuthenticationFilter(TokenHandler tokenHandler) {
        super(new AntPathRequestMatcher("/password-reset/confirmation"));
        this.tokenHandler = tokenHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String token = request.getParameter(QUERY_PARAMETER_TOKEN);
        if (Objects.isNull(token)) {
            throw new InsufficientAuthenticationException("Access token is missing");
        }

        TokenClaims claims = tokenHandler.parseToken(token);
        JWTAuthenticationToken authenticationToken = JWTAuthenticationToken.getBuilder()
                .withClaims(claims)
                .withRawToken(token)
                .build();

        return getAuthenticationManager().authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {

        // not letting the authentication success handler(s) to be called
        // in order to prevent redirecting back to the /oauth/authorize endpoint
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }
}
