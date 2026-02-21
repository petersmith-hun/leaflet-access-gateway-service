package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.UpdateRoleRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.UserManagementService;
import hu.psprog.leaflet.lags.web.security.Permit;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller endpoints for users operations.
 *
 * @author Peter Smith
 */
@Slf4j
@RestController
@RequestMapping("/access-management/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * Retrieves the given page of users.
     *
     * @param page 1-based page number, defaults to 0 (turns off pagination)
     * @return user details
     */
    @Permit.Read.Users
    @GetMapping
    public Page<UserDetailsResponse> getUsers(@RequestParam(value = "page", defaultValue = "0") int page) {
        return userManagementService.getUsers(page);
    }

    /**
     * Retrieves the details of the given user.
     *
     * @param userID user ID
     * @return user details
     */
    @Permit.Read.Users
    @GetMapping("/{userID}")
    public UserDetailsResponse getUser(@PathVariable Long userID) {
        return userManagementService.getUser(userID);
    }

    /**
     * Creates a new user.
     *
     * @param request user data
     * @return user details
     */
    @Permit.Write.Users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsResponse createUser(@Valid @RequestBody UserRequest request) {
        return userManagementService.createUser(request);
    }

    /**
     * Updates given user's role.
     *
     * @param userID user ID
     * @return user details
     */
    @Permit.Write.Users
    @PutMapping("/{userID}/role")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsResponse changeUserRole(@PathVariable Long userID, @RequestBody UpdateRoleRequestModel request) {
        return userManagementService.updateUserRole(userID, request.getRole());
    }

    /**
     * Enables the given user.
     *
     * @param userID user ID
     * @return user details
     */
    @Permit.Write.Users
    @PutMapping("/{userID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsResponse enableUser(@PathVariable Long userID) {
        return userManagementService.updateUserStatus(userID, true);
    }

    /**
     * Disables the given user.
     *
     * @param userID user ID
     * @return user details
     */
    @Permit.Write.Users
    @DeleteMapping("/{userID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDetailsResponse disableUser(@PathVariable Long userID) {
        return userManagementService.updateUserStatus(userID, false);
    }
}
