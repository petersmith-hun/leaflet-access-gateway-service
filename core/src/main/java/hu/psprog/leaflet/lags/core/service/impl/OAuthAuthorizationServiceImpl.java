package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.domain.response.UserInfoResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import hu.psprog.leaflet.lags.core.service.factory.OAuthRequestContextFactory;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult.FAILED_INTROSPECTION_RESULT;

/**
 * Implementation of {@link OAuthAuthorizationService}.
 * This implementation relies on the registered OAuth2 client (the one requesting the authorization) to be pre-authenticated.
 *
 * @author Peter Smith
 */
@Service
@Slf4j
public class OAuthAuthorizationServiceImpl implements OAuthAuthorizationService {

    private final Map<GrantType, GrantFlowProcessor> grantFlowProcessorMap;
    private final TokenHandler tokenHandler;
    private final AccessTokenDAO accessTokenDAO;
    private final OAuthRequestContextFactory oAuthRequestContextFactory;

    @Autowired
    public OAuthAuthorizationServiceImpl(List<GrantFlowProcessor> grantFlowProcessors, TokenHandler tokenHandler,
                                         AccessTokenDAO accessTokenDAO, OAuthRequestContextFactory oAuthRequestContextFactory) {

        this.grantFlowProcessorMap = grantFlowProcessors.stream()
                .collect(Collectors.toMap(GrantFlowProcessor::forGrantType, Function.identity()));
        this.tokenHandler = tokenHandler;
        this.accessTokenDAO = accessTokenDAO;
        this.oAuthRequestContextFactory = oAuthRequestContextFactory;
    }

    @Override
    public OAuthAuthorizationResponse authorize(OAuthAuthorizationRequest oAuthAuthorizationRequest) {

        OAuthAuthorizationRequestContext context = oAuthRequestContextFactory.createContext(oAuthAuthorizationRequest);
        log.info("OAuth authorization requested - grant={}; client={}; redirect={}",
                oAuthAuthorizationRequest.getGrantType(), oAuthAuthorizationRequest.getClientID(), oAuthAuthorizationRequest.getRedirectURI());

        return getResponsibleGrantFlowProcessor(oAuthAuthorizationRequest)
                .processAuthorizationRequest(context);
    }

    @Override
    public OAuthTokenResponse authorize(OAuthTokenRequest oAuthTokenRequest) {

        OAuthTokenRequestContext context = oAuthRequestContextFactory.createContext(oAuthTokenRequest);
        log.info("OAuth access token requested - grant={}; client={}; audience={}",
                oAuthTokenRequest.getGrantType(), oAuthTokenRequest.getClientID(), oAuthTokenRequest.getAudience());
        TokenClaims claims = getResponsibleGrantFlowProcessor(oAuthTokenRequest)
                .processTokenRequest(context);

        return tokenHandler.generateToken(oAuthTokenRequest, claims);
    }

    @Override
    public TokenIntrospectionResult introspect(String accessToken) {

        TokenIntrospectionResult introspectionResult;
        try {
            TokenClaims claims = tokenHandler.parseToken(accessToken);
            introspectionResult = TokenIntrospectionResult.builder()
                    .active(isTokenActive(claims))
                    .clientID(claims.getClientID())
                    .username(claims.getUsername())
                    .expiration(claims.getExpiration())
                    .build();
        } catch (Exception e) {
            introspectionResult = FAILED_INTROSPECTION_RESULT;
        }

        return introspectionResult;
    }

    @Override
    public UserInfoResponse getUserInfo(String accessToken) {

        TokenClaims claims = tokenHandler.parseToken(accessToken);

        return UserInfoResponse.builder()
                .sub(claims.getUserID().toString())
                .name(claims.getUsername())
                .email(claims.getEmail())
                .build();
    }

    private boolean isTokenActive(TokenClaims claims) {

        return accessTokenDAO.retrieveByJTI(claims.getTokenID())
                .map(accessTokenInfo -> TokenStatus.ACTIVE == accessTokenInfo.getStatus())
                .orElse(false);
    }

    private GrantFlowProcessor getResponsibleGrantFlowProcessor(OAuthRequest oAuthRequest) {

        GrantFlowProcessor grantFlowProcessor = grantFlowProcessorMap.get(oAuthRequest.getGrantType());
        if (Objects.isNull(grantFlowProcessor)) {
            throw new OAuthAuthorizationException(OAuthErrorCode.UNSUPPORTED_GRANT_TYPE,
                    String.format("OAuth authorization flow [%s] is not supported", oAuthRequest.getGrantType()));
        }

        return grantFlowProcessor;
    }
}
