package hu.psprog.leaflet.lags.core.domain.request;

import hu.psprog.leaflet.api.rest.request.user.UserPasswordRequestModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * Sign-up request model.
 * Includes ReCaptcha token field.
 *
 * @author Peter Smith
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignUpRequestModel extends UserPasswordRequestModel implements ReCaptchaProtectedRequest {

    @NotEmpty
    @Size(max = 255)
    private String username;

    @Email
    @NotEmpty
    @Size(max = 255)
    private String email;

    @NotEmpty
    private String recaptchaToken;

    private boolean validationFailed;

    @Override
    public String getRecaptchaToken() {
        return recaptchaToken;
    }
}
