package hu.psprog.leaflet.lags.core.domain.internal;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import lombok.Builder;
import lombok.Data;

/**
 * Context domain object containing the required data for processing an OAuth authorization request.
 * Contains the following objects:
 *  - The original {@link OAuthAuthorizationRequest} object;
 *  - The source OAuth client as {@link OAuthClient} object;
 *  - And the identified (signed in) user as {@link ExtendedUser} object.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OAuthAuthorizationRequestContext implements OAuthRequestContext {

    private final OAuthAuthorizationRequest request;
    private final OAuthClient sourceClient;
    private final ExtendedUser authenticatedUser;
}
