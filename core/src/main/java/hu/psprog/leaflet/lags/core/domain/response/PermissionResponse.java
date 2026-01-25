package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response model representing a permission.
 *
 * @param id generated internal ID
 * @param name name of the permission
 * @param description optional description
 * @param enabled flag indicating whether this permission can be selected in OAuth application or role definitions
 * @param created creation timestamp
 * @param lastModified last modification timestamp
 * @author Peter Smith
 */
@Builder
public record PermissionResponse(
        UUID id,
        String name,
        String description,
        boolean enabled,
        ZonedDateTime created,
        ZonedDateTime lastModified
) { }
