package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.internal.UserInfo;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.factory.OngoingAuthorizationFactory;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import hu.psprog.leaflet.lags.core.service.util.ScopeNegotiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private final OngoingAuthorizationFactory ongoingAuthorizationFactory;
    private final ScopeNegotiator scopeNegotiator;

    @Autowired
    public AuthorizationCodeGrantFlowProcessor(OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry, OngoingAuthorizationRepository ongoingAuthorizationRepository,
                                               OngoingAuthorizationFactory ongoingAuthorizationFactory, ScopeNegotiator scopeNegotiator) {
        super(oAuthRequestVerifierRegistry);
        this.ongoingAuthorizationRepository = ongoingAuthorizationRepository;
        this.ongoingAuthorizationFactory = ongoingAuthorizationFactory;
        this.scopeNegotiator = scopeNegotiator;
    }

    @Override
    public OAuthAuthorizationResponse processAuthorizationRequest(OAuthAuthorizationRequestContext context) {

        oAuthRequestVerifierRegistry.getAuthorizationRequestVerifiers()
                .forEach(verifier -> verifier.verify(context));

        OngoingAuthorization ongoingAuthorization = ongoingAuthorizationFactory.createOngoingAuthorization(context);
        ongoingAuthorizationRepository.saveOngoingAuthorization(ongoingAuthorization);

        return OAuthAuthorizationResponse.builder()
                .redirectURI(context.getRequest().getRedirectURI())
                .code(ongoingAuthorization.getAuthorizationCode())
                .state(context.getRequest().getState())
                .build();
    }

    @Override
    public GrantType forGrantType() {
        return GrantType.AUTHORIZATION_CODE;
    }

    @Override
    protected void doFlowSpecificTokenRequestContextProcessing(OAuthTokenRequestContext context) {
        context.getRequest().getScope().addAll(scopeNegotiator.getScope(context));
    }

    @Override
    protected TokenClaims.TokenClaimsBuilder generateCustomClaims(OAuthTokenRequestContext context) {

        UserInfo userInfo = context.getRequiredOngoingAuthorization().getUserInfo();
        TokenClaims.TokenClaimsBuilder tokenClaimsBuilder = super.generateCustomClaims(context)
                .subject(formatSubject(context, userInfo))
                .email(userInfo.getEmail())
                .role(userInfo.getRole())
                .username(userInfo.getUsername())
                .userID(userInfo.getId());

        ongoingAuthorizationRepository.deleteOngoingAuthorization(context.getRequest().getAuthorizationCode());

        return tokenClaimsBuilder;
    }

    private String formatSubject(OAuthTokenRequestContext context, UserInfo userInfo) {
        return String.format("%s|uid=%s", context.getSourceClient().getClientId(), userInfo.getId());
    }
}
