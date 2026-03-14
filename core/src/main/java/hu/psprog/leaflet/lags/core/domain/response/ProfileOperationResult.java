package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Getter;

/**
 * Enum constants indicating profile operation results. Result codes will be communicated to the frontend, so it can
 * render the corresponding notification.
 *
 * @author Peter Smith
 */
@Getter
public enum ProfileOperationResult {

    SUCCESS("success"),
    NEW_EMAIL_IN_USE("email-in-use"),
    CURRENT_PASSWORD_MISMATCH("current-password-mismatch"),
    NEW_PASSWORD_MISMATCH("new-password-mismatch"),
    UNKNOWN_ERROR("unknown-error");

    private final String resultCode;

    ProfileOperationResult(String resultCode) {
        this.resultCode = resultCode;
    }
}
