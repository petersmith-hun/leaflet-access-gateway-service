package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.util.ScopeNegotiator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link ScopeNegotiator} implementation for OAuth Authorization Code flow.
 *
 * Scope negotiation on the authorization request part happens as follows:
 *  - If the requested scope is empty, the scope assigned to the identified user is going to be authorized.
 *  - In this case, if the user doesn't have the required minimum scope (by the source client), the negotiation fails.
 *  - If the requested scope is defined, that is going to be authorized.
 *  - Similarly as in the other case, the user must have the defined set of scope.
 *
 * Scope negotiation on the token request part happens as follows:
 *  - If the relationship defines narrower scope than the authorized scope, and that scope is an exact subset of the authorized,
 *    scope will be dropped to the narrower set.
 *  - Otherwise, the intersection of the relationship-defined and authorized scope will be used.
 *
 * Note: the necessary pre- and post-verifications should always be executed, this component should NOT be relied on for proper scope verification.
 *
 * @author Peter Smith
 */
@Component
public class AuthorizationCodeFlowScopeNegotiator implements ScopeNegotiator {

    @Override
    public List<String> getScope(OAuthAuthorizationRequestContext context) {

        List<String> userAuthorities = getUserAuthorities(context);

        return StringUtils.isEmpty(context.getRequest().getScope())
                ? userAuthorities
                : getVerifiedDefinedScope(context, userAuthorities);
    }

    @Override
    public List<String> getScope(OAuthTokenRequestContext context) {

        if (!context.getRequest().getScope().isEmpty()) {
            throw new OAuthAuthorizationException("Token request should not specify scope on Authorization Code flow.");
        }

        return doesRelationHaveNarrowerScope(context)
                ? context.getRelation().getAllowedScopes()
                : getAlignedAuthorizedScope(context);
    }

    private List<String> getUserAuthorities(OAuthAuthorizationRequestContext context) {

        List<String> userAuthorities = context.getAuthenticatedUser()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        verifyRequestedScope(userAuthorities, context.getSourceClient().getRequiredScopes());

        return userAuthorities;
    }

    private List<String> getVerifiedDefinedScope(OAuthAuthorizationRequestContext context, List<String> userAuthorities) {

        List<String> definedScope = Arrays.asList(context.getRequest().getScopeAsArray());
        verifyRequestedScope(userAuthorities, definedScope);

        return definedScope;
    }

    private void verifyRequestedScope(List<String> userAuthorities, List<String> requestedScope) {

        if (!userAuthorities.containsAll(requestedScope)) {
            throw new OAuthAuthorizationException("Client requires broader authorities than what the user has.");
        }
    }

    private boolean doesRelationHaveNarrowerScope(OAuthTokenRequestContext context) {

        List<String> relationScope = context.getRelation().getAllowedScopes();
        List<String> authorizedScope = context.getRequiredOngoingAuthorization().getScope();

        return relationScope.size() < authorizedScope.size()
                && authorizedScope.containsAll(relationScope);
    }

    private List<String> getAlignedAuthorizedScope(OAuthTokenRequestContext context) {

        return context.getRequiredOngoingAuthorization()
                .getScope().stream()
                .filter(scope -> context.getRelation()
                        .getAllowedScopes().stream()
                        .anyMatch(scope::equals))
                .collect(Collectors.toList());
    }
}
