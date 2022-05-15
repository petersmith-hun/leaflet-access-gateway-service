package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.UserInfo;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@link AbstractGrantFlowProcessor} implementation for OAuth2 Authorization Code Flow authorization flow processing.
 * This implementation supports both main steps of the grant flow processor, since this one utilizes both the authorization
 * and the token request processing.
 *
 * While processing the authorization request, the implementation does the following:
 *  - Verifies the expected response type, application type, redirection, and scope demands.
 *  - Generates a random authorization code.
 *  - Generates an {@link OngoingAuthorization} object and stores it until the token request is processed.
 *  - Generates an {@link OAuthAuthorizationResponse} object that will be passed back to the source client application.
 *
 * While processing the corresponding token request, the following steps are executed:
 *  - Verifies the presence of the required request parameters.
 *  - Retrieves and verifies the {@link OngoingAuthorization} object based on the received authorization code.
 *  - Aligns scope demands before generating the token by matching up the requested (by source client),
 *    registered (in client and assigned to the user's role) and provided (by target client) scopes, and narrowing them
 *    down if necessary.
 *  - Generates the access token and returns it to the source client application.
 *  - Removes the corresponding {@link OngoingAuthorization} object from the storage.
 *
 * @author Peter Smith
 */
@Component
public class AuthorizationCodeGrantFlowProcessor extends AbstractGrantFlowProcessor {

    private final OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @Autowired
    public AuthorizationCodeGrantFlowProcessor(OAuthClientRegistry oAuthClientRegistry, OngoingAuthorizationRepository ongoingAuthorizationRepository) {
        super(oAuthClientRegistry);
        this.ongoingAuthorizationRepository = ongoingAuthorizationRepository;
    }

    @Override
    public OAuthAuthorizationResponse authorizeRequest(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient) {

        verifyResponseType(oAuthAuthorizationRequest);
        verifyApplicationType(oAuthClient);
        verifyRedirectURI(oAuthAuthorizationRequest, oAuthClient);
        verifyScope(oAuthAuthorizationRequest);

        OngoingAuthorization ongoingAuthorization = createOngoingAuthorization(oAuthAuthorizationRequest, oAuthClient);
        ongoingAuthorizationRepository.saveOngoingAuthorization(ongoingAuthorization);

        return OAuthAuthorizationResponse.builder()
                .redirectURI(oAuthAuthorizationRequest.getRedirectURI())
                .code(ongoingAuthorization.getAuthorizationCode())
                .state(oAuthAuthorizationRequest.getState())
                .build();
    }

    @Override
    public GrantType forGrantType() {
        return GrantType.AUTHORIZATION_CODE;
    }

    @Override
    protected void doFlowSpecificVerification(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient) {

        validateFieldExistence(oAuthTokenRequest, Map.of(
                OAuthConstants.Request.CODE, OAuthTokenRequest::getAuthorizationCode,
                OAuthConstants.Request.REDIRECT_URI, OAuthTokenRequest::getRedirectURI
        ));

        Optional<OngoingAuthorization> ongoingAuthorizationOptional =
                ongoingAuthorizationRepository.getOngoingAuthorizationByCode(oAuthTokenRequest.getAuthorizationCode());

        if (ongoingAuthorizationOptional.isPresent()) {

            OngoingAuthorization ongoingAuthorization = ongoingAuthorizationOptional.get();
            verifyOngoingAuthorization(ongoingAuthorization, oAuthTokenRequest);
            updateScope(ongoingAuthorization, oAuthTokenRequest);

        } else {
            throw new OAuthAuthorizationException("Unknown authorization request");
        }
    }

    @Override
    protected Map<String, Object> generateCustomClaims(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient) {

        Map<String, Object> claims = super.generateCustomClaims(oAuthTokenRequest, oAuthClient);

        Optional<OngoingAuthorization> ongoingAuthorizationOptional =
                ongoingAuthorizationRepository.getOngoingAuthorizationByCode(oAuthTokenRequest.getAuthorizationCode());

        ongoingAuthorizationOptional.ifPresent(ongoingAuthorization -> {
            UserInfo userInfo = ongoingAuthorization.getUserInfo();
            claims.put(OAuthConstants.Token.SUBJECT, String.format("%s|uid=%s", claims.get(OAuthConstants.Token.SUBJECT), userInfo.getId()));
            claims.put(OAuthConstants.Token.USER, userInfo.getEmail());
            claims.put(OAuthConstants.Token.ROLE, userInfo.getRole());
            claims.put(OAuthConstants.Token.NAME, userInfo.getUsername());
            claims.put(OAuthConstants.Token.USER_ID, userInfo.getId());
        });

        ongoingAuthorizationRepository.deleteOngoingAuthorization(oAuthTokenRequest.getAuthorizationCode());

        return claims;
    }

    private void verifyResponseType(OAuthAuthorizationRequest oAuthAuthorizationRequest) {

        if (oAuthAuthorizationRequest.getResponseType() != AuthorizationResponseType.CODE) {
            throw new OAuthAuthorizationException("Authorization response type must be [code]");
        }
    }

    private void verifyApplicationType(OAuthClient oAuthClient) {

        if (oAuthClient.getApplicationType() != ApplicationType.UI) {
            throw new OAuthAuthorizationException("Client application is not permitted to use authorization code flow.");
        }
    }

    private void verifyRedirectURI(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient) {

        if (!oAuthClient.getAllowedCallbacks().contains(oAuthAuthorizationRequest.getRedirectURI())) {
            throw new OAuthAuthorizationException(String.format("Specified redirection URI [%s] is not registered", oAuthAuthorizationRequest.getRedirectURI()));
        }
    }

    private void verifyScope(OAuthAuthorizationRequest oAuthAuthorizationRequest) {

        if (!StringUtils.isEmpty(oAuthAuthorizationRequest.getScope())) {
            ExtendedUser userDetails = getUserDetails();
            List<GrantedAuthority> requestedScopes = AuthorityUtils.createAuthorityList(oAuthAuthorizationRequest.getScopeAsArray());

            if (!userDetails.getAuthorities().containsAll(requestedScopes)) {
                throw new OAuthAuthorizationException("Requested scope is broader than the user's authority range.");
            }
        }
    }

    private OngoingAuthorization createOngoingAuthorization(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient) {

        ExtendedUser userDetails = getUserDetails();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(1L);

        return OngoingAuthorization.builder()
                .authorizationCode(UUID.randomUUID().toString())
                .clientID(oAuthAuthorizationRequest.getClientID())
                .redirectURI(oAuthAuthorizationRequest.getRedirectURI())
                .userInfo(UserInfo.builder()
                        .id(userDetails.getId())
                        .email(userDetails.getUsername())
                        .username(userDetails.getName())
                        .role(userDetails.getRole())
                        .build())
                .expiration(expiration)
                .scope(getScope(oAuthAuthorizationRequest, oAuthClient))
                .build();
    }

    private List<String> getScope(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient) {

        List<String> scope;

        if (StringUtils.isEmpty(oAuthAuthorizationRequest.getScope())) {

            List<String> userAuthorities = getUserDetails().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            if (userAuthorities.containsAll(oAuthClient.getRegisteredScopes())) {
                scope = oAuthClient.getRegisteredScopes();
            } else {
                throw new OAuthAuthorizationException("Client requires broader authorities than what the user has.");
            }

        } else {
            scope = Arrays.asList(oAuthAuthorizationRequest.getScopeAsArray());
        }

        return scope;
    }

    private ExtendedUser getUserDetails() {
        return (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void verifyOngoingAuthorization(OngoingAuthorization ongoingAuthorization, OAuthTokenRequest oAuthTokenRequest) {

        if (!ongoingAuthorization.getClientID().equals(oAuthTokenRequest.getClientID())) {
            throw new OAuthAuthorizationException("Authorization request belongs to a different client.");
        }

        if (!ongoingAuthorization.getRedirectURI().equals(oAuthTokenRequest.getRedirectURI())) {
            throw new OAuthAuthorizationException("Different redirect URI has been specified in the token request.");
        }

        if (ongoingAuthorization.getExpiration().isBefore(LocalDateTime.now())) {
            ongoingAuthorizationRepository.deleteOngoingAuthorization(ongoingAuthorization.getAuthorizationCode());
            throw new OAuthAuthorizationException("Authorization has already expired.");
        }
    }

    private void updateScope(OngoingAuthorization ongoingAuthorization, OAuthTokenRequest oAuthTokenRequest) {

        if (!oAuthTokenRequest.getScope().isEmpty()) {
            throw new OAuthAuthorizationException("Token request should not specify scope on Authorization Code flow.");
        }

        oAuthTokenRequest.getScope().addAll(ongoingAuthorization.getScope());
    }
}
