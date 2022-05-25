package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Internal request domain class representing the base information of an access token to be tracked.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class StoreAccessTokenInfoRequest {

    private final String id;
    private final String subject;
    private final Date issuedAt;
    private final Date expiresAt;
}
