package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Domain class representing an access token's claims.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class TokenClaims {

    private final String tokenID;
    private final String username;
    private final String email;
    private final String clientID;
    private final Date expiration;
    private final String audience;
    private final String[] scopes;
}
