package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Abstract implementation of {@link GrantFlowProcessor} adding basic and common verification steps and utilities
 * for specific flow processors.
 *
 * Default verification steps includes:
 *  - validation for client ID and audience fields (both must be present to continue processing);
 *  - flow specific additional verification (by calling doFlowSpecificVerification method);
 *  - target client existence;
 *  - relation check between source and target client (target allows access for any of its resources to source?);
 *  - requested scope check (requested scope is the same or a subset of the allowed scopes for the source client, defined by the relation definition).
 *
 * @author Peter Smith
 */
abstract class AbstractGrantFlowProcessor implements GrantFlowProcessor {

    private final OAuthClientRegistry oAuthClientRegistry;

    protected AbstractGrantFlowProcessor(OAuthClientRegistry oAuthClientRegistry) {
        this.oAuthClientRegistry = oAuthClientRegistry;
    }

    @Override
    public OAuthAuthorizationResponse authorizeRequest(OAuthAuthorizationRequest oAuthAuthorizationRequest, OAuthClient oAuthClient) {
        throw new UnsupportedOperationException("Authorization is not supported on this grant flow.");
    }

    @Override
    public Map<String, Object> verifyRequest(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient) {

        validateFieldExistence(oAuthTokenRequest, Map.of(
                OAuthConstants.Request.CLIENT_ID, OAuthTokenRequest::getClientID,
                OAuthConstants.Request.AUDIENCE, OAuthTokenRequest::getAudience
        ));

        doFlowSpecificVerification(oAuthTokenRequest, oAuthClient);
        OAuthClient targetClient = verifyTargetClient(oAuthTokenRequest);
        OAuthClientAllowRelation clientAllowRelation = verifyRelation(oAuthClient, targetClient);
        verifyScope(oAuthTokenRequest, oAuthClient, targetClient, clientAllowRelation);

        return generateCustomClaims(oAuthTokenRequest, oAuthClient);
    }

    /**
     * Executes flow specific verification steps prior to every common verification steps.
     * Implementations of this method should include only those verification steps not present in the common steps.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param oAuthClient source client descriptor based on the request
     */
    protected abstract void doFlowSpecificVerification(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient);

    /**
     * Finalization step of the grant flow verification. Generates and returns custom claims to be added to the
     * generated JWT token. This default implementations adds the "scope" and "sub" (subject) JWT token claims.
     * Concrete implementations should any flow specific additional token claims.
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param oAuthClient source client descriptor based on the request
     * @return generated custom token claims as map
     */
    protected Map<String, Object> generateCustomClaims(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(OAuthConstants.Token.SCOPE, String.join(StringUtils.SPACE, oAuthTokenRequest.getScope()));
        claims.put(OAuthConstants.Token.SUBJECT, oAuthClient.getClientId());

        return claims;
    }

    /**
     * Validates if the specified fields in the {@link OAuthTokenRequest} are not blank (null or empty string).
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param fieldMappers field mapper function to extract specific fields of the {@link OAuthTokenRequest} object
     */
    protected final void validateFieldExistence(OAuthTokenRequest oAuthTokenRequest, Map<String, Function<OAuthTokenRequest, String>> fieldMappers) {

        fieldMappers.forEach((fieldName, valueMapper) -> {
            String value = valueMapper.apply(oAuthTokenRequest);
            if (StringUtils.isEmpty(value)) {
                throw new OAuthAuthorizationException(String.format("Value for required authorization parameter [%s] is missing", fieldName));
            }
        });
    }

    private OAuthClient verifyTargetClient(OAuthTokenRequest oAuthTokenRequest) {

        return oAuthClientRegistry.getClientByAudience(oAuthTokenRequest.getAudience())
                .orElseThrow(() -> new OAuthAuthorizationException(String.format("Requested access for non-registered OAuth client [%s]", oAuthTokenRequest.getAudience())));
    }

    private OAuthClientAllowRelation verifyRelation(OAuthClient oAuthClient, OAuthClient targetClient) {

        return targetClient.getAllowedClients().stream()
                .filter(relation -> relation.getName().equals(oAuthClient.getClientName()))
                .findFirst()
                .orElseThrow(() -> new OAuthAuthorizationException(String.format("Target client [%s] does not allow access for source client [%s]",
                        targetClient.getClientName(), oAuthClient.getClientName())));
    }

    private void verifyScope(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient, OAuthClient targetClient, OAuthClientAllowRelation clientAllowRelation) {

        boolean requestedScopeIsAllowed = clientAllowRelation.getAllowedScopes().containsAll(oAuthTokenRequest.getScope());
        if (!requestedScopeIsAllowed) {
            throw new OAuthAuthorizationException(String.format("Target client [%s] does not allow the requested scope [%s] for source client [%s]",
                    targetClient.getClientName(), oAuthTokenRequest.getScope(), oAuthClient.getClientName()));
        }
    }
}
