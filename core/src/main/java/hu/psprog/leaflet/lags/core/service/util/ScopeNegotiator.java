package hu.psprog.leaflet.lags.core.service.util;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;

import java.util.List;

/**
 * Helper interface for scope negotiation.
 * Implementations should be able to "calculate" the proper (sub)set of the requested/available scope during authorization.
 *
 * @author Peter Smith
 */
public interface ScopeNegotiator {

    /**
     * Negotiates scope during the authorization request part of an authorization flow.
     *
     * @param context {@link OAuthAuthorizationRequestContext} object containing the authorization request parameters
     * @return list of negotiated scopes
     */
    List<String> getScope(OAuthAuthorizationRequestContext context);

    /**
     * Negotiates scope during the token request part of an authorization flow.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     * @return list of negotiated scopes
     */
    List<String> getScope(OAuthTokenRequestContext context);
}
