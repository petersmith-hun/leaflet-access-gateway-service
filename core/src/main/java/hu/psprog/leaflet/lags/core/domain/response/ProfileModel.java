package hu.psprog.leaflet.lags.core.domain.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

/**
 * Request model for profile update operation.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class ProfileModel {

    @NotEmpty
    @Size(max = 255)
    private String username;

    @Email
    @NotEmpty
    @Size(max = 255)
    private String email;

    @NotNull
    private String locale;
}
