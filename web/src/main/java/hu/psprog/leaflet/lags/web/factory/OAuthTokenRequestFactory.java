package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Factory component to create {@link OAuthTokenRequest} objects out of the received OAuth2 token request parameters,
 * and the current {@link UserDetails} object stored in the Security Context.
 *
 * @author Peter Smith
 */
@Component
public class OAuthTokenRequestFactory {

    /**
     * Creates an {@link OAuthTokenRequest}.
     * Extracts the necessary parameters from the request parameters and wraps them as a domain class.
     *
     * Also does some further steps:
     *  - if client ID is only passed in the {@link UserDetails} object (but missing in the request parameters), copies it over;
     *  - otherwise (if client ID is present in both the {@link UserDetails} object and in the request parameters), verifies if they are same;
     *  - latter check triggers an {@link OAuthAuthorizationException} if it fails.
     *
     * @param requestParameters OAuth2 authorization request parameters coming from a form POST HTTP request
     * @param userDetails {@link UserDetails} object stored in the Security Context
     * @return the created {@link OAuthTokenRequest} object
     */
    public OAuthTokenRequest createTokenRequest(Map<String, String> requestParameters, UserDetails userDetails) {

        if (Objects.isNull(requestParameters.get(OAuthConstants.Request.CLIENT_ID))) {
            requestParameters.put(OAuthConstants.Request.CLIENT_ID, userDetails.getUsername());
        } else if (!requestParameters.get(OAuthConstants.Request.CLIENT_ID).equals(userDetails.getUsername())) {
            throw new OAuthAuthorizationException("Authenticated client is different than the one the authorization is requested for");
        }

        return OAuthTokenRequest.builder()
                .grantType(GrantType.parseGrantType(requestParameters.get(OAuthConstants.Request.GRANT_TYPE)))
                .clientID(requestParameters.get(OAuthConstants.Request.CLIENT_ID))
                .username(requestParameters.get(OAuthConstants.Request.USERNAME))
                .password(requestParameters.get(OAuthConstants.Request.PASSWORD))
                .audience(requestParameters.get(OAuthConstants.Request.AUDIENCE))
                .authorizationCode(requestParameters.get(OAuthConstants.Request.CODE))
                .redirectURI(requestParameters.get(OAuthConstants.Request.REDIRECT_URI))
                .scope(extractScope(requestParameters))
                .build();
    }

    private List<String> extractScope(Map<String, String> requestParameters) {

        return Optional.ofNullable(requestParameters.get(OAuthConstants.Request.SCOPE))
                .map(scope -> scope.split(StringUtils.SPACE))
                .map(Arrays::asList)
                .orElseGet(LinkedList::new);
    }
}
