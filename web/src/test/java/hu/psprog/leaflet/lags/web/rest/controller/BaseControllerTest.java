package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.exception.ExpiredTokenException;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.core.exception.RevokedTokenException;
import hu.psprog.leaflet.lags.web.model.AuthorizationError;
import hu.psprog.leaflet.lags.web.model.TokenRequestError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link BaseController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class BaseControllerTest {

    @Mock
    private OAuthConfigurationProperties oAuthConfigurationProperties;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private BaseController baseController;

    @Test
    public void shouldHandleAuthenticationExceptionRenderAccessDeniedPageForExpiredToken() {

        // given
        Exception exception = new ExpiredTokenException();
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";

        given(request.getAttribute("SPRING_SECURITY_LAST_EXCEPTION")).willReturn(exception);
        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleAuthenticationException(request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.ACCESS_DENIED,
                "error_description", "Your password reset token has expired, please request a new one",
                "redirect_uri", redirectURI
        )));
    }

    @Test
    public void shouldHandleAuthenticationExceptionRenderAccessDeniedPageForAnyOtherAuthenticationError() {

        // given
        Exception exception = new BadCredentialsException("Bad credentials");
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";

        given(request.getAttribute("SPRING_SECURITY_LAST_EXCEPTION")).willReturn(exception);
        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleAuthenticationException(request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.ACCESS_DENIED,
                "error_description", "Access denied",
                "redirect_uri", redirectURI
        )));
    }


    @Test
    public void shouldHandleErrorRenderServerErrorPage() {

        // given
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";

        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleError(request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.SERVER_ERROR,
                "error_description", "Unknown error",
                "redirect_uri", redirectURI
        )));
    }

    @Test
    public void shouldHandleAuthorizationExceptionWithoutRedirectURI() {

        // given
        String exceptionMessage = "OAuth authorization failed - Access Denied";
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";
        OAuthAuthorizationException exception = new OAuthAuthorizationException(exceptionMessage);

        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleAuthorizationException(exception, request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.ACCESS_DENIED,
                "error_description", exceptionMessage,
                "redirect_uri", redirectURI
        )));

        verify(request).getParameter("redirect_uri");
    }

    @ParameterizedTest
    @EnumSource(OAuthErrorCode.class)
    public void shouldHandleTokenRequestExceptionGenerateProperResponseEntity(OAuthErrorCode errorCode) {

        // given
        String message = String.format("Token request exception - %s", errorCode.getErrorCode());
        OAuthTokenRequestException exception = new OAuthTokenRequestException(errorCode, message);

        // when
        ResponseEntity<TokenRequestError> result = baseController.handleTokenRequestException(exception);

        // then
        assertThat(result.getStatusCode(), equalTo(errorCode.getMappedStatus()));
        assertThat(result.getBody(), notNullValue());
        assertThat(result.getBody().errorCode(), equalTo(errorCode.getErrorCode()));
        assertThat(result.getBody().errorDescription(), equalTo(message));
    }

    @Test
    public void shouldHandleAuthorizationExceptionWithRedirectURI() {

        // given
        String exceptionMessage = "OAuth authorization failed - Invalid scope";
        String redirectURI = "http://localhost:9999/callback/provided?auth=failed";
        OAuthAuthorizationException exception = new OAuthAuthorizationException(OAuthErrorCode.INVALID_SCOPE, exceptionMessage);

        given(request.getParameter("redirect_uri")).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleAuthorizationException(exception, request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.INVALID_SCOPE,
                "error_description", exceptionMessage,
                "redirect_uri", redirectURI
        )));

        verify(request).getParameter("redirect_uri");
    }

    @Test
    public void shouldHandleAuthorizationExceptionWithInvalidRedirectURI() {

        // given
        String exceptionMessage = "OAuth authorization failed - Invalid grant";
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";
        OAuthAuthorizationException exception = new OAuthAuthorizationException(OAuthErrorCode.INVALID_GRANT, exceptionMessage);

        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleAuthorizationException(exception, request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.INVALID_GRANT,
                "error_description", exceptionMessage,
                "redirect_uri", redirectURI
        )));

        verify(request, never()).getParameter("redirect_uri");
    }

    @ParameterizedTest
    @ValueSource(classes = {
            AuthenticationException.class,
            RevokedTokenException.class
    })
    public void shouldHandleAuthenticationException(Class<? extends Exception> exceptionClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        // given
        String exceptionMessage = "Authentication failed";
        Exception exception = exceptionClass.getConstructor(String.class).newInstance(exceptionMessage);
        AuthorizationError expectedAuthorizationError = new AuthorizationError(exceptionMessage);

        // when
        ResponseEntity<AuthorizationError> result = baseController.handleAuthenticationException(exception);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.UNAUTHORIZED));
        assertThat(result.getBody(), equalTo(expectedAuthorizationError));
    }

    @Test
    public void shouldHandleExceptionRenderErrorPage() {

        // given
        Exception exception = new IllegalArgumentException("Illegal argument");
        String redirectURI = "http://localhost:9999/callback/default?auth=failed";

        given(oAuthConfigurationProperties.getDefaultRedirectOnError()).willReturn(redirectURI);

        // when
        ModelAndView result = baseController.handleException(exception, request);

        // then
        assertThat(result.getViewName(), equalTo("views/error"));
        assertThat(result.getModel().size(), equalTo(3));
        assertThat(result.getModel(), equalTo(Map.of(
                "error_code", OAuthErrorCode.SERVER_ERROR,
                "error_description", "Unknown error",
                "redirect_uri", redirectURI
        )));
    }
}
