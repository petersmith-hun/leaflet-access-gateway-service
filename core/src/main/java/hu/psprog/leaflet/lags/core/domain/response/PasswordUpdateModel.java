package hu.psprog.leaflet.lags.core.domain.response;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

/**
 * Request model for password update operation.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class PasswordUpdateModel {

    @NotEmpty
    private String currentPassword;

    @NotEmpty
    private String newPassword;

    @NotEmpty
    private String newPasswordConfirmation;
}
