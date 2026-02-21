package hu.psprog.leaflet.lags.core.domain.request;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request model representing a new user.
 *
 * @author Peter Smith
 */
@Builder
public record UserRequest(

        @NotNull
        @NotEmpty
        @Size(max = 255)
        String username,

        @NotNull
        @Email
        @NotEmpty
        @Size(max = 255)
        String email,

        @NotNull
        SupportedLocale defaultLocale,

        @NotNull
        LegacyRole role
) { }
