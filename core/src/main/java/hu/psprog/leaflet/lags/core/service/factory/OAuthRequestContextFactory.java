package hu.psprog.leaflet.lags.core.service.factory;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;

/**
 * Factory component for creating request context objects.
 *
 * @author Peter Smith
 */
public interface OAuthRequestContextFactory {

    /**
     * Creates an {@link OAuthAuthorizationRequestContext} object from the given {@link OAuthAuthorizationRequest}.
     *
     * @param oAuthAuthorizationRequest {@link OAuthAuthorizationRequest} object to be wrapped as {@link OAuthAuthorizationRequestContext}
     * @return created {@link OAuthAuthorizationRequestContext} object
     */
    OAuthAuthorizationRequestContext createContext(OAuthAuthorizationRequest oAuthAuthorizationRequest);

    /**
     * Creates an {@link OAuthTokenRequestContext} object from the given {@link OAuthTokenRequest}.
     *
     * @param oAuthTokenRequest {@link OAuthTokenRequest} object to be wrapped as {@link OAuthTokenRequestContext}
     * @return created {@link OAuthTokenRequestContext} object
     */
    OAuthTokenRequestContext createContext(OAuthTokenRequest oAuthTokenRequest);
}
