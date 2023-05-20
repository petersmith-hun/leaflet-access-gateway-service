package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.JWTAuthenticationToken;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.domain.response.UserInfoResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.web.factory.OAuthAuthorizationRequestFactory;
import hu.psprog.leaflet.lags.web.factory.OAuthTokenRequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants.PATH_OAUTH_USERINFO;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_AUTHORIZE;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_INTROSPECT;
import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_OAUTH_TOKEN;

/**
 * Controller implementation for OAuth2 authentication related endpoints.
 *
 * @author Peter Smith
 */
@RestController
@Slf4j
public class OAuth2AuthenticationController {

    private static final String VIEW_AUTHORIZE = "views/authorize";
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

        log.info("User requested authorization via client={}", request.getParameter(OAuthConstants.Request.CLIENT_ID));

        ExtendedUser userDetails = (ExtendedUser) authentication.getPrincipal();

        return new ModelAndView(VIEW_AUTHORIZE, Map.of(
                "name", userDetails.getName(),
                "email", userDetails.getUsername(),
                "authorizedScope", extractScope(userDetails),
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

        log.info("Authorization started for client={}", requestParameters.get(OAuthConstants.Request.CLIENT_ID));

        OAuthAuthorizationRequest oAuthAuthorizationRequest = oAuthAuthorizationRequestFactory.createAuthorizationRequest(requestParameters);
        OAuthAuthorizationResponse oAuthAuthorizationResponse = oAuthAuthorizationService.authorize(oAuthAuthorizationRequest);

        return new ModelAndView(String.format(REDIRECT_URL_TEMPLATE,
                oAuthAuthorizationResponse.redirectURI(), oAuthAuthorizationResponse.code(), oAuthAuthorizationResponse.state()));
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

        log.info("Access token requested by client={}", requestParameters.get(OAuthConstants.Request.CLIENT_ID));

        OAuthTokenRequest oAuthTokenRequest = oAuthTokenRequestFactory.createTokenRequest(requestParameters, (UserDetails) authentication.getPrincipal());
        OAuthTokenResponse oAuthTokenResponse = oAuthAuthorizationService.authorize(oAuthTokenRequest);

        return createResponse(oAuthTokenResponse);
    }

    /**
     * POST /oauth/introspect
     * Processes a token introspection request. Introspection can be used to check if the given token is tracked and is not yet revoked.
     *
     * @param token access token to be introspected
     * @param authentication current {@link Authentication} object
     * @return introspection results as {@link TokenIntrospectionResult} object wrapped in {@link ResponseEntity}
     */
    @PostMapping(PATH_OAUTH_INTROSPECT)
    public ResponseEntity<TokenIntrospectionResult> introspectToken(@RequestParam String token, Authentication authentication) {

        log.info("Token introspection requested by client={}", authentication.getName());

        return ResponseEntity.ok(oAuthAuthorizationService.introspect(token));
    }

    /**
     * GET /oauth/userinfo
     * Returns a user's information based on their access token.
     *
     * @param authentication object of type {@link Authentication} to extract the user information
     * @return extracted user information
     */
    @GetMapping(PATH_OAUTH_USERINFO)
    public ResponseEntity<UserInfoResponse> getUserInfo(Authentication authentication) {

        JWTAuthenticationToken token = (JWTAuthenticationToken) authentication;
        String accessToken = token.getCredentials().toString();

        log.info("User info requested by client={}", token.getDetails().getSubject());

        return ResponseEntity.ok(oAuthAuthorizationService.getUserInfo(accessToken));
    }

    private List<String> extractScope(ExtendedUser userDetails) {

        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
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
