package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import java.util.UUID;

/**
 * User management operations.
 *
 * @author Peter Smith
 */
public interface UserManagementService {

    /**
     * Retrieves the given page of users.
     *
     * @param page 1-based page number
     * @return user details
     */
    Page<UserDetailsResponse> getUsers(int page);

    /**
     * Retrieves the details of the given user.
     *
     * @param userID user ID
     * @return user details
     */
    UserDetailsResponse getUser(Long userID);

    /**
     * Creates a new user.
     *
     * @param user user data
     * @return user details
     */
    UserDetailsResponse createUser(UserRequest user);

    /**
     * Updates the assigned role of the given user.
     *
     * @param userID user ID to update role of
     * @param roleID ID of the target role to assign to the user
     * @return user details
     */
    UserDetailsResponse updateUserRole(Long userID, UUID roleID);

    /**
     * Enables/disables the given user.
     *
     * @param userID user ID
     * @param enabled target status
     * @return user details
     */
    UserDetailsResponse updateUserStatus(Long userID, boolean enabled);

    /**
     * Updates the last login of the given user to the current timestamp.
     *
     * @param authentication {@link Authentication} object to extract user information from
     */
    void updateLastLogin(Authentication authentication);
}
