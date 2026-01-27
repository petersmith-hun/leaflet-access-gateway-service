package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.domain.response.UserInfoResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.web.factory.OAuthAuthorizationRequestFactory;
import hu.psprog.leaflet.lags.web.factory.OAuthTokenRequestFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
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

    private static final String REQUEST_URL = "https://dev.local:9999/authorize";
    private static final String QUERY_STRING = "response_type=code&client_id=client-1";
    private static final String EXPECTED_LOGOUT_REF = REQUEST_URL + "?" + QUERY_STRING;
    private static final String ACCESS_TOKEN = "access-token-1";
    private static final Map<String, String> REQUEST_PARAMETERS = prepareRequestParameters();
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST = prepareTokenRequest();
    private static final OAuthTokenResponse O_AUTH_TOKEN_RESPONSE = prepareTokenResponse();
    private static final ExtendedUser EXTENDED_USER = prepareExtendedUser();
    private static final TokenIntrospectionResult TOKEN_INTROSPECTION_RESULT = prepareTokenIntrospectionResult();
    private static final JwtAuthenticationToken JWT_AUTHENTICATION_TOKEN = prepareJwtAuthenticationToken();
    private static final UserInfoResponse USER_INFO_RESPONSE = prepareUserInfoResponse();

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
        assertThat(result.getModel().get("authorizedScope"), equalTo(Arrays.asList("write:admin", "read:admin")));
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
        given(authentication.getName()).willReturn("client_id_1");

        // when
        ResponseEntity<TokenIntrospectionResult> result = oAuth2AuthenticationController.introspectToken(ACCESS_TOKEN, authentication);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), equalTo(TOKEN_INTROSPECTION_RESULT));
    }

    @Test
    public void shouldGetUserInfoReturnUserInfoExtractedFromAuthenticationObject() {

        // given
        given(oAuthAuthorizationService.getUserInfo(JWT_AUTHENTICATION_TOKEN.getToken())).willReturn(USER_INFO_RESPONSE);

        // when
        ResponseEntity<UserInfoResponse> result = oAuth2AuthenticationController.getUserInfo(JWT_AUTHENTICATION_TOKEN);

        // then
        assertThat(result.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(result.getBody(), equalTo(USER_INFO_RESPONSE));
    }

    private void assertLogoutRef(ModelAndView result) {

        String encodedLogoutRef = result.getModel().get("logoutRef").toString();
        String decodedLogoutRef = new String(Base64.getDecoder().decode(encodedLogoutRef.getBytes()));

        assertThat(decodedLogoutRef, equalTo(EXPECTED_LOGOUT_REF));
    }

    private static Map<String, String> prepareRequestParameters() {
        return Map.of(
                "grant_type", "password"
        );
    }

    private static OAuthTokenRequest prepareTokenRequest() {
        return OAuthTokenRequest.builder()
                .grantType(GrantType.PASSWORD)
                .build();
    }

    private static OAuthTokenResponse prepareTokenResponse() {
        return OAuthTokenResponse.builder()
                .accessToken("token1")
                .build();
    }

    private static ExtendedUser prepareExtendedUser() {
        return ExtendedUser.builder()
                .name("name1")
                .username("email@dev.local")
                .authorities(AuthorityUtils.createAuthorityList("write:admin", "read:admin"))
                .build();
    }

    private static TokenIntrospectionResult prepareTokenIntrospectionResult() {
        return TokenIntrospectionResult.builder()
                .active(true)
                .expiration(new Date())
                .build();
    }

    private static JwtAuthenticationToken prepareJwtAuthenticationToken() {

        Jwt token = Jwt.withTokenValue(ACCESS_TOKEN)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(20L))
                .subject("unit-test")
                .header("alg", "HS256")
                .build();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("read:all"));

        return new JwtAuthenticationToken(token, authorities);
    }

    private static UserInfoResponse prepareUserInfoResponse() {

        return UserInfoResponse.builder()
                .sub("1234")
                .email("email@dev.local")
                .name("username-1")
                .build();
    }
}
