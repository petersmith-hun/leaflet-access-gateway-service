package hu.psprog.leaflet.lags.core.domain.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Response model representing a permission.
 *
 * @param name unique identifier name of the permission
 * @param description optional description of the permission
 */
@Builder
@Jacksonized
public record PermissionRequest(

        @NotEmpty
        @Pattern(regexp = "^[a-z][a-z:_-]+[a-z]$", message = "Must match pattern: ^[a-z][a-z:_-]+[a-z]$")
        String name,

        String description
) { }
