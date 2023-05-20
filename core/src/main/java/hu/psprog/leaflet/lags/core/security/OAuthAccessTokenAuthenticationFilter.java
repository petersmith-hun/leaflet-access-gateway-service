package hu.psprog.leaflet.lags.core.security;

import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.exception.ExpiredTokenException;
import hu.psprog.leaflet.lags.core.exception.JWTTokenParsingException;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

/**
 * {@link AbstractAuthenticationProcessingFilter} implementation to verify and authenticate access token secured requests.
 *
 * Activates only on the given endpoints. For the successful authentication, the filter extracts the access token from
 * the given source (defined by the extraction function).
 *
 * @author Peter Smith
 */
public class OAuthAccessTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final TokenHandler tokenHandler;
    private final Function<HttpServletRequest, String> tokenExtractionFunction;

    public OAuthAccessTokenAuthenticationFilter(TokenHandler tokenHandler, String pathPattern, Function<HttpServletRequest, String> tokenExtractionFunction) {
        super(new AntPathRequestMatcher(pathPattern));
        this.tokenHandler = tokenHandler;
        this.tokenExtractionFunction = tokenExtractionFunction;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String token = tokenExtractionFunction.apply(request);
        if (Objects.isNull(token)) {
            throw new InsufficientAuthenticationException("Access token is missing");
        }

        if (token.startsWith(BEARER_TOKEN_PREFIX)) {
            token = token.substring(BEARER_TOKEN_PREFIX.length());
        }

        JWTAuthenticationToken authenticationToken;
        try {
            TokenClaims claims = tokenHandler.parseToken(token);
            authenticationToken = JWTAuthenticationToken.getBuilder()
                    .withClaims(claims)
                    .withRawToken(token)
                    .build();
        } catch (JWTTokenParsingException exception) {
            throw new ExpiredTokenException();
        } catch (Exception exception) {
            throw new BadCredentialsException("Invalid credentials", exception);
        }

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
