package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.web.factory.OAuthTokenRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller implementation for OAuth2 authentication related endpoints.
 *
 * @author Peter Smith
 */
@RestController
@RequestMapping("/oauth")
public class OAuth2AuthenticationController {

    private final OAuthTokenRequestFactory oAuthTokenRequestFactory;
    private final OAuthAuthorizationService oAuthAuthorizationService;

    @Autowired
    public OAuth2AuthenticationController(OAuthTokenRequestFactory oAuthTokenRequestFactory, OAuthAuthorizationService oAuthAuthorizationService) {
        this.oAuthTokenRequestFactory = oAuthTokenRequestFactory;
        this.oAuthAuthorizationService = oAuthAuthorizationService;
    }

    /**
     * POST /oauth/token.
     * Handles OAuth2 access token requests. This endpoint requires pre-authentication by client in order to handle the request properly.
     *
     * @param requestParameters OAuth2 authorization request parameters coming from a form POST HTTP request
     * @param authentication {@link Authentication} object containing the result of the client pre-authentication
     * @return response entity containing the generated access token with HTTP 200 OK status.
     */
    @PostMapping("/token")
    public ResponseEntity<OAuthTokenResponse> claimToken(@RequestParam Map<String, String> requestParameters, Authentication authentication) {

        OAuthTokenRequest oAuthTokenRequest = oAuthTokenRequestFactory.createTokenRequest(requestParameters, (UserDetails) authentication.getPrincipal());
        OAuthTokenResponse oAuthTokenResponse = oAuthAuthorizationService.authorize(oAuthTokenRequest);

        return createResponse(oAuthTokenResponse);
    }

    private <T> ResponseEntity<T> createResponse(T response) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Pragma", "no-cache")
                .body(response);
    }
}
