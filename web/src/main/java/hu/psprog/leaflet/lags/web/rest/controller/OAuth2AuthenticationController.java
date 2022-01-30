package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.web.factory.OAuthAuthorizationRequestFactory;
import hu.psprog.leaflet.lags.web.factory.OAuthTokenRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_AUTHORIZE;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_TOKEN;

/**
 * Controller implementation for OAuth2 authentication related endpoints.
 *
 * @author Peter Smith
 */
@RestController
public class OAuth2AuthenticationController {

    private static final String VIEW_AUTHORIZE = "authorize";
    private static final String REDIRECT_URL_TEMPLATE = "redirect:%s?code=%s&state=%s";
    private static final String LOGOUT_REFERENCE_URL_TEMPLATE = "%s?%s";

    private final OAuthTokenRequestFactory oAuthTokenRequestFactory;
    private final OAuthAuthorizationRequestFactory oAuthAuthorizationRequestFactory;
    private final OAuthAuthorizationService oAuthAuthorizationService;

    @Autowired
    public OAuth2AuthenticationController(OAuthTokenRequestFactory oAuthTokenRequestFactory, OAuthAuthorizationRequestFactory oAuthAuthorizationRequestFactory,
                                          OAuthAuthorizationService oAuthAuthorizationService) {
        this.oAuthTokenRequestFactory = oAuthTokenRequestFactory;
        this.oAuthAuthorizationRequestFactory = oAuthAuthorizationRequestFactory;
        this.oAuthAuthorizationService = oAuthAuthorizationService;
    }

    /**
     * GET /oauth/authorize
     * Renders the authorization form. User must already be authenticated when hitting this endpoint.
     *
     * The form contains a logout references, generated upon calling this endpoint, which can be used to redirect the user
     * back to this form (with all the necessary authorization parameters) in case the user switches account.
     *
     * @param request {@link HttpServletRequest} object to form the logout reference
     * @param authentication {@link Authentication} object to extract some user information to be shown on the form
     * @return populated {@link ModelAndView} object
     */
    @GetMapping(PATH_OAUTH_AUTHORIZE)
    public ModelAndView renderAuthorizationForm(HttpServletRequest request, Authentication authentication) {

        ExtendedUser userDetails = (ExtendedUser) authentication.getPrincipal();

        return new ModelAndView(VIEW_AUTHORIZE, Map.of(
                "name", userDetails.getName(),
                "email", userDetails.getUsername(),
                "logoutRef", createLogoutReference(request)
        ));
    }

    /**
     * POST /oauth/authorize
     * Processes the given authorization request according to OAuth2 Authorization Code Flow.
     * User being authenticated is still a must when calling this endpoint.
     *
     * @param requestParameters authorization request parameters as a single {@link Map}
     * @return redirection {@link ModelAndView} object to return the user to the client application (on success)
     */
    @PostMapping(PATH_OAUTH_AUTHORIZE)
    public ModelAndView processAuthorizationRequest(@RequestParam Map<String, String> requestParameters) {

        OAuthAuthorizationRequest oAuthAuthorizationRequest = oAuthAuthorizationRequestFactory.createAuthorizationRequest(requestParameters);
        OAuthAuthorizationResponse oAuthAuthorizationResponse = oAuthAuthorizationService.authorize(oAuthAuthorizationRequest);

        return new ModelAndView(String.format(REDIRECT_URL_TEMPLATE,
                oAuthAuthorizationResponse.getRedirectURI(), oAuthAuthorizationResponse.getCode(), oAuthAuthorizationResponse.getState()));
    }

    /**
     * POST /oauth/token
     * Handles OAuth2 access token requests. This endpoint requires pre-authentication by client in order to handle the request properly.
     *
     * @param requestParameters OAuth2 authorization request parameters coming from a form POST HTTP request
     * @param authentication {@link Authentication} object containing the result of the client pre-authentication
     * @return response entity containing the generated access token with HTTP 200 OK status.
     */
    @PostMapping(PATH_OAUTH_TOKEN)
    public ResponseEntity<OAuthTokenResponse> claimToken(@RequestParam Map<String, String> requestParameters, Authentication authentication) {

        OAuthTokenRequest oAuthTokenRequest = oAuthTokenRequestFactory.createTokenRequest(requestParameters, (UserDetails) authentication.getPrincipal());
        OAuthTokenResponse oAuthTokenResponse = oAuthAuthorizationService.authorize(oAuthTokenRequest);

        return createResponse(oAuthTokenResponse);
    }

    private String createLogoutReference(HttpServletRequest request) {

        byte[] redirectURL = String.format(LOGOUT_REFERENCE_URL_TEMPLATE, request.getRequestURL(), request.getQueryString())
                .getBytes(StandardCharsets.UTF_8);

        return Base64.getEncoder()
                .encodeToString(redirectURL);
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
