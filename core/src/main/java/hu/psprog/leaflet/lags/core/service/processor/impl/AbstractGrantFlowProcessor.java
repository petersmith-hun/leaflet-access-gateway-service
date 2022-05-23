package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.apache.commons.lang3.StringUtils;

/**
 * Abstract implementation of {@link GrantFlowProcessor} adding basic and common verification steps and utilities
 * for specific flow processors.
 *
 * Default verification steps include:
 *  - validation for client ID and audience fields (both must be present to continue processing);
 *  - flow specific additional verification (by calling doFlowSpecificVerification method);
 *  - target client existence;
 *  - relation check between source and target client (target allows access for any of its resources to source?);
 *  - requested scope check (requested scope is the same or a subset of the allowed scopes for the source client, defined by the relation definition).
 *
 * @author Peter Smith
 */
abstract class AbstractGrantFlowProcessor implements GrantFlowProcessor {

    protected final OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry;

    protected AbstractGrantFlowProcessor(OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry) {
        this.oAuthRequestVerifierRegistry = oAuthRequestVerifierRegistry;
    }

    @Override
    public OAuthAuthorizationResponse processAuthorizationRequest(OAuthAuthorizationRequestContext context) {
        throw new UnsupportedOperationException("Authorization is not supported on this grant flow.");
    }

    @Override
    public TokenClaims processTokenRequest(OAuthTokenRequestContext context) {

        doFlowSpecificTokenRequestContextProcessing(context);
        oAuthRequestVerifierRegistry.getTokenRequestVerifiers(forGrantType())
                .forEach(verifier -> verifier.verify(context));
        doFlowSpecificTokenRequestContextVerification(context);

        return generateCustomClaims(context).build();
    }

    /**
     * Optional request processing steps to be done before the {@link OAuthTokenRequestContext} verification.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     */
    protected void doFlowSpecificTokenRequestContextProcessing(OAuthTokenRequestContext context) {
    }

    /**
     * Executes flow specific verification steps after every common verification steps.
     * Implementations of this method should include only those verification steps not present in the common steps.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     */
    protected void doFlowSpecificTokenRequestContextVerification(OAuthTokenRequestContext context) {
    }

    /**
     * Finalization step of the grant flow verification. Generates and returns custom claims to be added to the
     * generated JWT token. This default implementations adds the "scope" and "sub" (subject) JWT token claims.
     * Concrete implementations should any flow specific additional token claims.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     * @return generated custom token claims as map
     */
    protected TokenClaims.TokenClaimsBuilder generateCustomClaims(OAuthTokenRequestContext context) {

        return TokenClaims.builder()
                .scope(String.join(StringUtils.SPACE, context.getRequest().getScope()))
                .subject(context.getSourceClient().getClientId());
    }
}
