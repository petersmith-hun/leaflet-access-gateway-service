package hu.psprog.leaflet.lags.core.domain.response;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response model representing the summary of an OAuth application definition (for listing definitions).
 *
 * @param id generated internal ID of the application
 * @param name unique name
 * @param clientID unique OAuth client ID
 * @param applicationType internal OAuth application type (primary OAuth role)
 * @param enabled determines if the definition can be requested by OAuth application logins
 * @param created creation timestamp
 * @param lastModified last modification timestamp
 */
@Builder
public record OAuthApplicationSummaryResponse(
        UUID id,
        String name,
        String clientID,
        ApplicationType applicationType,
        boolean enabled,
        ZonedDateTime created,
        ZonedDateTime lastModified
) { }
