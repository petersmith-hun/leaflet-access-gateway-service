package hu.psprog.leaflet.lags.core.service.factory;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;

/**
 * Factory component for generating {@link OngoingAuthorization} objects.
 *
 * @author Peter Smith
 */
public interface OngoingAuthorizationFactory {

    /**
     * Creates an {@link OngoingAuthorization} object based on the given {@link OAuthAuthorizationRequestContext}.
     *
     * @param context {@link OAuthAuthorizationRequestContext} containing the required data for generating the {@link OngoingAuthorization} object
     * @return created {@link OngoingAuthorization} object
     */
    OngoingAuthorization createOngoingAuthorization(OAuthAuthorizationRequestContext context);
}
