package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * {@link OAuthTokenRequestFieldExistenceVerifier} implementation for the token requests on all flows.
 * The implementation verifies the following aspects:
 *  - Existence of the Client ID field (must be specified);
 *  - Existence of the Audience field (must be specified);
 *  - Scope (target client must allow the requested scope to the source client).
 *
 * @author Peter Smith
 */
@Component
public class CommonTokenOAuthRequestVerifier extends OAuthTokenRequestFieldExistenceVerifier {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.values());

    @Override
    public void verify(OAuthTokenRequestContext context) {

        verifyFieldExistence(context);
        verifyScope(context);
    }

    @Override
    public List<GrantType> forGrantType() {
        return GRANT_TYPES;
    }

    private void verifyFieldExistence(OAuthTokenRequestContext context) {

        verifyFieldExistence(context.getRequest(), Map.of(
                OAuthConstants.Request.CLIENT_ID, OAuthTokenRequest::getClientID,
                OAuthConstants.Request.AUDIENCE, OAuthTokenRequest::getAudience
        ));
    }

    private void verifyScope(OAuthTokenRequestContext context) {

        boolean requestedScopeIsAllowed = context.getRelation().getAllowedScopes().containsAll(context.getRequest().getScope());
        if (!requestedScopeIsAllowed) {
            throw new OAuthAuthorizationException(String.format("Target client [%s] does not allow the requested scope [%s] for source client [%s]",
                    context.getTargetClient().getClientName(), context.getRequest().getScope(), context.getSourceClient().getClientName()));
        }
    }
}
