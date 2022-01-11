package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.token.TokenGenerator;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link OAuthAuthorizationService}.
 * This implementation relies on the registered OAuth2 client (the one requesting the authorization) to be pre-authenticated.
 *
 * @author Peter Smith
 */
@Service
public class OAuthAuthorizationServiceImpl implements OAuthAuthorizationService {

    private final Map<GrantType, GrantFlowProcessor> grantFlowProcessorMap;
    private final OAuthClientRegistry oAuthClientRegistry;
    private final TokenGenerator tokenGenerator;

    @Autowired
    public OAuthAuthorizationServiceImpl(List<GrantFlowProcessor> grantFlowProcessors, OAuthClientRegistry oAuthClientRegistry,
                                         TokenGenerator tokenGenerator) {

        this.grantFlowProcessorMap = grantFlowProcessors.stream()
                .collect(Collectors.toMap(GrantFlowProcessor::forGrantType, Function.identity()));
        this.oAuthClientRegistry = oAuthClientRegistry;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public OAuthTokenResponse authorize(OAuthTokenRequest oAuthTokenRequest) {

        OAuthClient oAuthClient = getOAuthClient(oAuthTokenRequest);
        Map<String, Object> claims = getResponsibleGrantFlowProcessor(oAuthTokenRequest)
                .verifyRequest(oAuthTokenRequest, oAuthClient);

        return tokenGenerator.generateToken(oAuthTokenRequest, claims);
    }

    private OAuthClient getOAuthClient(OAuthTokenRequest oAuthTokenRequest) {

        return oAuthClientRegistry.getClientByClientID(oAuthTokenRequest.getClientID())
                .orElseThrow(() -> new OAuthAuthorizationException(String.format("OAuth client by ID [%s] is not registered", oAuthTokenRequest.getClientID())));
    }

    private GrantFlowProcessor getResponsibleGrantFlowProcessor(OAuthTokenRequest oAuthTokenRequest) {

        GrantFlowProcessor grantFlowProcessor = grantFlowProcessorMap.get(oAuthTokenRequest.getGrantType());
        if (Objects.isNull(grantFlowProcessor)) {
            throw new OAuthAuthorizationException(String.format("OAuth authorization flow [%s] is not supported", oAuthTokenRequest.getGrantType()));
        }

        return grantFlowProcessor;
    }
}
