package hu.psprog.leaflet.lags.core.domain.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * Password reset request model.
 * Includes ReCaptcha token field.
 *
 * @author Peter Smith
 */
@Data
public class PasswordResetRequestModel implements ReCaptchaProtectedRequest {

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String recaptchaToken;

    private boolean validationFailed;
}
