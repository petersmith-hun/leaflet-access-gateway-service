package hu.psprog.leaflet.lags.web.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum constants defining user profile operation screens and their parameters.
 *
 * @author Peter Smith
 */
@Getter
@RequiredArgsConstructor
public enum ProfileScreen {

    LOGIN(null, null, "redirect:/login"),
    MAIN("views/profile_main", null, "redirect:/profile?operation=%s"),
    EDIT_ACCOUNT("views/profile_edit_account", "edit-account", "redirect:/profile/edit-account?status=%s"),
    CHANGE_PASSWORD("views/profile_change_password", "change-password", "redirect:/profile/change-password?status=%s"),
    DELETE_ACCOUNT("views/profile_delete_account", "delete-account", "redirect:/profile/delete-account?status=%s");

    /**
     * View path of this screen (under resources directory).
     */
    private final String viewPath;

    /**
     * Operation code to identify notifications to be shown after supported operations.
     */
    private final String operationCode;

    /**
     * Target URLs to redirect the browser to after a successful or failed profile operation.
     */
    private final String redirectUrl;
}
