package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * {@link OAuthRequestVerifier} implementation for OAuth Authorization Code flow authorization request verification.
 * The implementation verifies the following aspects:
 *  - The application type of the source OAuth client (must be UI);
 *  - The requested redirect URI by the source OAuth client;
 *  - The request response type (must be CODE);
 *  - And the requested scope.
 *
 * @author Peter Smith
 */
@Component
public class AuthCodeAuthorizationOAuthRequestVerifier implements OAuthRequestVerifier<OAuthAuthorizationRequestContext> {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.AUTHORIZATION_CODE);

    @Override
    public void verify(OAuthAuthorizationRequestContext context) {

        verifyApplicationType(context);
        verifyRedirectURI(context);
        verifyResponseType(context);
        verifyScope(context);
    }

    @Override
    public List<GrantType> forGrantType() {
        return GRANT_TYPES;
    }

    private void verifyApplicationType(OAuthAuthorizationRequestContext context) {

        if (context.getSourceClient().getApplicationType() != ApplicationType.UI) {
            throw new OAuthAuthorizationException("Client application is not permitted to use authorization code flow.");
        }
    }

    private void verifyRedirectURI(OAuthAuthorizationRequestContext context) {

        if (!context.getSourceClient().getAllowedCallbacks().contains(context.getRequest().getRedirectURI())) {
            throw new OAuthAuthorizationException(String.format("Specified redirection URI [%s] is not registered", context.getRequest().getRedirectURI()));
        }
    }

    private void verifyResponseType(OAuthAuthorizationRequestContext context) {

        if (context.getRequest().getResponseType() != AuthorizationResponseType.CODE) {
            throw new OAuthAuthorizationException("Authorization response type must be [code]");
        }
    }

    private void verifyScope(OAuthAuthorizationRequestContext context) {

        if (!StringUtils.isEmpty(context.getRequest().getScope())) {
            List<GrantedAuthority> requestedScopes = AuthorityUtils.createAuthorityList(context.getRequest().getScopeAsArray());

            if (!context.getAuthenticatedUser().getAuthorities().containsAll(requestedScopes)) {
                throw new OAuthAuthorizationException("Requested scope is broader than the user's authority range.");
            }
        }
    }
}
