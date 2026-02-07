package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Role operations.
 *
 * @author Peter Smith
 */
public interface RoleService {

    /**
     * Retrieves the given page of roles.
     *
     * @param page 1-based page number
     * @return role details
     */
    Page<RoleResponse> getRoles(int page);

    /**
     * Retrieves the details of the given role.
     *
     * @param roleID role ID
     * @return role details
     */
    RoleResponse getRole(UUID roleID);

    /**
     * Creates a new role.
     *
     * @param role role data
     * @return role details
     */
    RoleResponse createRole(RoleRequest role);

    /**
     * Edits an existing role.
     *
     * @param roleID role ID
     * @param role role data
     * @return role details
     */
    RoleResponse editRole(UUID roleID, RoleRequest role);

    /**
     * Enables/disables the given role.
     *
     * @param roleID role ID
     * @param enabled target status
     * @return role details
     */
    RoleResponse updateRoleStatus(UUID roleID, boolean enabled);

    /**
     * Marks the given role as default for local user registrations.
     *
     * @param roleID role ID
     * @return role details
     */
    RoleResponse markAsLocalDefault(UUID roleID);

    /**
     * Marks the given role as default for external user registrations.
     *
     * @param roleID role ID
     * @return role details
     */
    RoleResponse markAsExternalDefault(UUID roleID);

    /**
     * Assigns the given permission to the given role.
     *
     * @param roleID role ID
     * @param permissionID permission ID
     */
    RoleResponse assignPermission(UUID roleID, UUID permissionID);

    /**
     * Unassigns the given permission from the given role.
     *
     * @param roleID role ID
     * @param permissionID permission ID
     */
    RoleResponse unassignPermission(UUID roleID, UUID permissionID);

    /**
     * Removes an existing role.
     *
     * @param roleID role ID
     */
    void deleteRole(UUID roleID);
}
