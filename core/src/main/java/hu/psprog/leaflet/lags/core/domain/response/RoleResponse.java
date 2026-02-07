package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response model representing a role.
 *
 * @param id generated internal ID
 * @param name name of the role
 * @param description optional description
 * @param localDefault flag indicating that the permission should be set as default role for new local users
 * @param externalDefault flag indicating that the permission should be set as default role for new external users
 * @param enabled flag indicating whether this role can be selected for users
 * @param created creation timestamp
 * @param lastModified last modification timestamp
 * @param permissions permissions assigned to this role
 * @author Peter Smith
 */
@Builder
public record RoleResponse(
        UUID id,
        String name,
        String description,
        boolean localDefault,
        boolean externalDefault,
        boolean enabled,
        ZonedDateTime created,
        ZonedDateTime lastModified,
        List<PermissionResponse> permissions
) { }
