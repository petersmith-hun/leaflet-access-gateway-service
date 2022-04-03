package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.web.factory.OAuthAuthorizationRequestFactory;
import hu.psprog.leaflet.lags.web.factory.OAuthTokenRequestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuth2AuthenticationController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationControllerTest {

    private static final Map<String, String> REQUEST_PARAMETERS = Map.of(
            "grant_type", "password"
    );
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST = OAuthTokenRequest.builder()
            .grantType(GrantType.PASSWORD)
            .build();
    private static final OAuthTokenResponse O_AUTH_TOKEN_RESPONSE = OAuthTokenResponse.builder()
            .accessToken("token1")
            .build();
    private static final ExtendedUser EXTENDED_USER = ExtendedUser.builder()
            .name("name1")
            .username("email@dev.local")
            .build();
    private static final String REQUEST_URL = "https://dev.local:9999/authorize";
    private static final String QUERY_STRING = "response_type=code&client_id=client-1";
    private static final String EXPECTED_LOGOUT_REF = REQUEST_URL + "?" + QUERY_STRING;
    private static final String ACCESS_TOKEN = "access-token-1";
    private static final TokenIntrospectionResult TOKEN_INTROSPECTION_RESULT = TokenIntrospectionResult.builder()
            .active(true)
            .expiration(new Date())
            .build();

    @Mock
    private OAuthTokenRequestFactory oAuthTokenRequestFactory;

    @Mock
    private OAuthAuthorizationRequestFactory oAuthAuthorizationRequestFactory;

    @Mock
    private OAuthAuthorizationService oAuthAuthorizationService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OAuth2AuthenticationController oAuth2AuthenticationController;

    @Test
    public void shouldRenderAuthorizationFormReturnPopulatedModelAndView() {

        // given
        given(authentication.getPrincipal()).willReturn(EXTENDED_USER);
        given(request.getRequestURL()).willReturn(new StringBuffer(REQUEST_URL));
        given(request.getQueryString()).willReturn(QUERY_STRING);

        // when
        ModelAndView result = oAuth2AuthenticationController.renderAuthorizationForm(request, authentication);

        // then
        assertThat(result.getViewName(), equalTo("views/authorize"));
        assertThat(result.getModel().get("name"), equalTo(EXTENDED_USER.getName()));
        assertThat(result.getModel().get("email"), equalTo(EXTENDED_USER.getUsername()));
        assertLogoutRef(result);
    }

    @Test
    public void shouldProcessAuthorizationRequestReturnProperRedirectionModelAndView() {

        // given
        Map<String, String> requestParameters = Map.of("response_type", "code");
        OAuthAuthorizationRequest oAuthAuthorizationRequest = OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.CODE)
                .build();
        OAuthAuthorizationResponse oAuthAuthorizationResponse = OAuthAuthorizationResponse.builder()
                .code("code-1")
                .state("state-1")
                .redirectURI("https://dev.local:9999/callback")
                .build();

        given(oAuthAuthorizationRequestFactory.createAuthorizationRequest(requestParameters)).willReturn(oAuthAuthorizationRequest);
        given(oAuthAuthorizationService.authorize(oAuthAuthorizationRequest)).willReturn(oAuthAuthorizationResponse);

        // when
        ModelAndView result = oAuth2AuthenticationController.processAuthorizationRequest(requestParameters);

        // then
        assertThat(result.getViewName(), equalTo("redirect:https://dev.local:9999/callback?code=code-1&state=state-1"));
    }

    @Test
    public void shouldClaimTokenProcessRequest() {

        // given
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(oAuthTokenRequestFactory.createTokenRequest(REQUEST_PARAMETERS, userDetails)).willReturn(O_AUTH_TOKEN_REQUEST);
        given(oAuthAuthorizationService.authorize(O_AUTH_TOKEN_REQUEST)).willReturn(O_AUTH_TOKEN_RESPONSE);

        // when
        ResponseEntity<OAuthTokenResponse> result = oAuth2AuthenticationController.claimToken(REQUEST_PARAMETERS, authentication);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getHeaders().getFirst("Cache-Control"), equalTo("no-store"));
        assertThat(result.getHeaders().getFirst("Content-Type"), equalTo("application/json"));
        assertThat(result.getHeaders().getFirst("Pragma"), equalTo("no-cache"));
        assertThat(result.getBody(), equalTo(O_AUTH_TOKEN_RESPONSE));
    }

    @Test
    public void shouldIntrospectTokenProcessRequest() {

        // given
        given(oAuthAuthorizationService.introspect(ACCESS_TOKEN)).willReturn(TOKEN_INTROSPECTION_RESULT);

        // when
        ResponseEntity<TokenIntrospectionResult> result = oAuth2AuthenticationController.introspectToken(ACCESS_TOKEN);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), equalTo(TOKEN_INTROSPECTION_RESULT));
    }

    private void assertLogoutRef(ModelAndView result) {

        String encodedLogoutRef = result.getModel().get("logoutRef").toString();
        String decodedLogoutRef = new String(Base64.getDecoder().decode(encodedLogoutRef.getBytes()));

        assertThat(decodedLogoutRef, equalTo(EXPECTED_LOGOUT_REF));
    }
}
