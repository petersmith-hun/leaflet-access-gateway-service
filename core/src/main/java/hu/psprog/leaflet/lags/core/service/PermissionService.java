package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Permission operations.
 *
 * @author Peter Smith
 */
public interface PermissionService {

    /**
     * Retrieves the given page of permissions.
     *
     * @param page 1-based page number
     * @return permission details
     */
    Page<PermissionResponse> getPermissions(int page);

    /**
     * Retrieves the details of the given permission.
     *
     * @param permissionID permission ID
     * @return permission details
     */
    PermissionResponse getPermission(UUID permissionID);

    /**
     * Creates a new permission.
     *
     * @param permission permission data
     * @return permission details
     */
    PermissionResponse createPermission(PermissionRequest permission);

    /**
     * Edits an existing permission.
     *
     * @param permissionID permission ID
     * @param permission permission data
     * @return permission details
     */
    PermissionResponse editPermission(UUID permissionID, PermissionRequest permission);

    /**
     * Enables/disables the given permission.
     *
     * @param permissionID permission ID
     * @param enabled target status
     * @return permission details
     */
    PermissionResponse updatePermissionStatus(UUID permissionID, boolean enabled);

    /**
     * Removes an existing permission.
     *
     * @param permissionID permission ID
     */
    void deletePermission(UUID permissionID);
}
