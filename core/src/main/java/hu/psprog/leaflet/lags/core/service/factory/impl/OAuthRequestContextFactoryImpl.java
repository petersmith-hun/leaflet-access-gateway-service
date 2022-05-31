package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.factory.OAuthRequestContextFactory;
import hu.psprog.leaflet.lags.core.service.registry.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Implementation of {@link OAuthRequestContextFactory}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthRequestContextFactoryImpl implements OAuthRequestContextFactory {

    private final OAuthClientRegistry oAuthClientRegistry;
    private final OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @Autowired
    public OAuthRequestContextFactoryImpl(OAuthClientRegistry oAuthClientRegistry, OngoingAuthorizationRepository ongoingAuthorizationRepository) {
        this.oAuthClientRegistry = oAuthClientRegistry;
        this.ongoingAuthorizationRepository = ongoingAuthorizationRepository;
    }

    @Override
    public OAuthAuthorizationRequestContext createContext(OAuthAuthorizationRequest oAuthAuthorizationRequest) {

        return OAuthAuthorizationRequestContext.builder()
                .request(oAuthAuthorizationRequest)
                .sourceClient(getSourceOAuthClient(oAuthAuthorizationRequest, OAuthAuthorizationException::new))
                .authenticatedUser(getUserDetails())
                .build();
    }

    @Override
    public OAuthTokenRequestContext createContext(OAuthTokenRequest oAuthTokenRequest) {

        OAuthClient sourceClient = getSourceOAuthClient(oAuthTokenRequest, OAuthTokenRequestException::new);
        OAuthClient targetClient = getTargetOAuthClient(oAuthTokenRequest);
        OAuthClientAllowRelation relation = getRelation(sourceClient, targetClient);

        return OAuthTokenRequestContext.builder()
                .request(oAuthTokenRequest)
                .sourceClient(sourceClient)
                .targetClient(targetClient)
                .relation(relation)
                .ongoingAuthorization(Optional.ofNullable(oAuthTokenRequest.getAuthorizationCode())
                        .flatMap(ongoingAuthorizationRepository::getOngoingAuthorizationByCode))
                .build();
    }

    private OAuthClient getSourceOAuthClient(OAuthRequest oAuthRequest, BiFunction<OAuthErrorCode, String, ? extends OAuthAuthorizationException> exceptionFunction) {

        return oAuthClientRegistry.getClientByClientID(oAuthRequest.getClientID())
                .orElseThrow(() -> exceptionFunction.apply(OAuthErrorCode.INVALID_CLIENT,
                        String.format("OAuth client by ID [%s] is not registered", oAuthRequest.getClientID())));
    }

    private OAuthClient getTargetOAuthClient(OAuthTokenRequest oAuthTokenRequest) {

        return oAuthClientRegistry.getClientByAudience(oAuthTokenRequest.getAudience())
                .orElseThrow(() -> new OAuthTokenRequestException(OAuthErrorCode.UNAUTHORIZED_CLIENT,
                        String.format("Requested access for non-registered OAuth client [%s]", oAuthTokenRequest.getAudience())));
    }

    private OAuthClientAllowRelation getRelation(OAuthClient sourceClient, OAuthClient targetClient) {

        return targetClient.getAllowedClients().stream()
                .filter(relation -> relation.getName().equals(sourceClient.getClientName()))
                .findFirst()
                .orElseThrow(() -> new OAuthTokenRequestException(OAuthErrorCode.UNAUTHORIZED_CLIENT,
                        String.format("Target client [%s] does not allow access for source client [%s]", targetClient.getClientName(), sourceClient.getClientName())));
    }

    private ExtendedUser getUserDetails() {
        return (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
