package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
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

    @Mock
    private OAuthTokenRequestFactory oAuthTokenRequestFactory;

    @Mock
    private OAuthAuthorizationService oAuthAuthorizationService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private OAuth2AuthenticationController oAuth2AuthenticationController;

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
}
