package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;

/**
 * Domain class for holding pieces of user information for the OAuth2 user info endpoint.
 *
 * @author Peter Smith
 */
@Builder
public record UserInfoResponse(
        String sub,
        String name,
        String email
) { }
