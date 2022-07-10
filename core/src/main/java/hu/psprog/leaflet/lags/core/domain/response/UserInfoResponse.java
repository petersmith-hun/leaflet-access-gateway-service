package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;
import lombok.Data;

/**
 * Domain class for holding pieces of user information for the OAuth2 user info endpoint.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class UserInfoResponse {

    private final String sub;
    private final String name;
    private final String email;
}
