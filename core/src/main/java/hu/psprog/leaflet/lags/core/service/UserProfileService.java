package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.response.PasswordUpdateModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import org.springframework.security.core.Authentication;

/**
 * User profile operations.
 *
 * @author Peter Smith
 */
public interface UserProfileService {

    /**
     * Retrieves profile data based on the current {@link Authentication} object (i.e. the active user).
     *
     * @param authentication {@link Authentication} object containing the user's ID to resolve the active user
     * @return resolved profile data as {@link ProfileModel}
     */
    ProfileModel getProfile(Authentication authentication);

    /**
     * Retrieves detailed user data based on the current {@link Authentication} object (i.e. the active user).
     *
     * @param authentication {@link Authentication} object containing the user's ID to resolve the active user
     * @return resolved user details as {@link UserDetailsResponse}
     */
    UserDetailsResponse getUserDetails(Authentication authentication);

    /**
     * Updates the current (local) user's profile information. External users are not allowed to modify their profile data.
     *
     * @param authentication {@link Authentication} object containing the user's ID to resolve the active user
     * @param profileModel new profile data
     * @return profile update result as {@link ProfileOperationResult}
     */
    ProfileOperationResult updateProfile(Authentication authentication, ProfileModel profileModel);

    /**
     * Updates the current (local) user's password. External users don't have a local password, therefore they cannot update it.
     *
     * @param authentication {@link Authentication} object containing the user's ID to resolve the active user
     * @param passwordUpdateModel new password data (current password, new password and its confirmation)
     * @return profile update result as {@link ProfileOperationResult}
     */
    ProfileOperationResult updatePassword(Authentication authentication, PasswordUpdateModel passwordUpdateModel);

    /**
     * Deletes the current user's profile.
     *
     * @param authentication {@link Authentication} object containing the user's ID to resolve the active user
     * @param currentPassword current password of the active user to confirm their intention of deleting their profile
     * @return profile update result as {@link ProfileOperationResult}
     */
    ProfileOperationResult deleteAccount(Authentication authentication, String currentPassword);
}
