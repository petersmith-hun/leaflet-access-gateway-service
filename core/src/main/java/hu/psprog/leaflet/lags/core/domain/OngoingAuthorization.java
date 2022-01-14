package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain class holding information about an in-progress OAuth2 authorization.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class OngoingAuthorization {

    private final String authorizationCode;
    private final String clientID;
    private final String redirectURI;
    private final UserInfo userInfo;
    private final LocalDateTime expiration;
    private final List<String> scope;
    private final boolean requestedScope;
}
