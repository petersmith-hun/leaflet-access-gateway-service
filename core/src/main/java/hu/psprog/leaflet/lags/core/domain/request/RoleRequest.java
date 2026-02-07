package hu.psprog.leaflet.lags.core.domain.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Response model representing a role.
 *
 * @param name role name
 * @param description optional role description
 * @author Peter Smith
 */
@Builder
@Jacksonized
public record RoleRequest(

        @NotEmpty
        String name,

        String description
) { }
