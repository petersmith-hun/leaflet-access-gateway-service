package hu.psprog.leaflet.lags.core.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

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

    /**
     * Creates a password reset request for internal usage (when user is created via the user administration API).
     *
     * @param userRequest user creation request containing the email address to request password reset for
     * @return created {@link PasswordResetRequestModel}
     */
    public static PasswordResetRequestModel internal(UserRequest userRequest) {

        PasswordResetRequestModel passwordResetRequestModel = new PasswordResetRequestModel();
        passwordResetRequestModel.email = userRequest.email();

        return passwordResetRequestModel;
    }
}
