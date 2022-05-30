package hu.psprog.leaflet.lags.core.domain.internal;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

/**
 * Context domain object containing the required data for processing an OAuth token request.
 * Contains the following objects:
 *  - The original {@link OAuthTokenRequest};
 *  - The source and the target OAuth clients extracted from the request as {@link OAuthClient} objects;
 *  - The relation descriptor between the clients as {@link OAuthClientAllowRelation} object;
 *  - And an {@link OngoingAuthorization} object wrapped in {@link Optional} for Authorization Code flow based authorizations.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthTokenRequestContext implements OAuthRequestContext {

    private final OAuthTokenRequest request;
    private final OAuthClient sourceClient;
    private final OAuthClient targetClient;
    private final OAuthClientAllowRelation relation;
    private final Optional<OngoingAuthorization> ongoingAuthorization;

    /**
     * Returns the stored {@link OngoingAuthorization} object for token requests on Auth Code flow.
     * Throws exception if missing (call the standard getter method instead if the object is not required).
     *
     * @return the stored {@link OngoingAuthorization} object
     * @throws OAuthAuthorizationException if the {@link OngoingAuthorization} object is not set
     */
    public OngoingAuthorization getRequiredOngoingAuthorization() {

        return ongoingAuthorization
                .orElseThrow(() -> new OAuthTokenRequestException(OAuthErrorCode.INVALID_REQUEST, "Missing ongoing authorization"));
    }
}
