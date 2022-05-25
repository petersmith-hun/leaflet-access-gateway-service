package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.Builder;
import lombok.Data;

/**
 * Domain class for holding information about the authorized user on Authorization Code Flow.
 * These pieces of information will be stored until the access token is requested.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class UserInfo {

    private final Long id;
    private final String username;
    private final String email;
    private final String role;
}
