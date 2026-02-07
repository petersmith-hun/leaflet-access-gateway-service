package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import hu.psprog.leaflet.lags.core.service.RoleService;
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

import java.util.UUID;

/**
 * REST controller endpoints for role operations.
 *
 * @author Peter Smith
 */
@Slf4j
@RestController
@RequestMapping("/access-management/roles")
public class RoleManagementController extends BaseManagementController {

    private final RoleService roleService;

    @Autowired
    public RoleManagementController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves the given page of roles.
     *
     * @param page 1-based page number, defaults to 0 (turns off pagination)
     * @return role details
     */
    @Permit.Read.Roles
    @GetMapping
    public Page<RoleResponse> getRoles(@RequestParam(value = "page", defaultValue = "0") int page) {
        return roleService.getRoles(page);
    }

    /**
     * Retrieves the details of the given role.
     *
     * @param roleID role ID
     * @return role details
     */
    @Permit.Read.Roles
    @GetMapping("/{roleID}")
    public RoleResponse getRole(@PathVariable UUID roleID) {
        return roleService.getRole(roleID);
    }

    /**
     * Creates a new role.
     *
     * @param request role data
     * @return role details
     */
    @Permit.Write.Roles
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) {
        return roleService.createRole(request);
    }

    /**
     * Edits an existing role.
     *
     * @param roleID role ID
     * @param request role data
     * @return role details
     */
    @Permit.Write.Roles
    @PutMapping("/{roleID}")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse editRole(@PathVariable UUID roleID, @Valid @RequestBody RoleRequest request) {
        return roleService.editRole(roleID, request);
    }

    /**
     * Enables the given role.
     *
     * @param roleID role ID
     * @return role details
     */
    @Permit.Write.Roles
    @PutMapping("/{roleID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse enableRole(@PathVariable UUID roleID) {
        return roleService.updateRoleStatus(roleID, true);
    }

    /**
     * Disables the given role.
     *
     * @param roleID role ID
     * @return role details
     */
    @Permit.Write.Roles
    @DeleteMapping("/{roleID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse disableRole(@PathVariable UUID roleID) {
        return roleService.updateRoleStatus(roleID, false);
    }

    /**
     * Marks the given role as default for locally registered users.
     *
     * @param roleID role ID
     * @return role details
     */
    @Permit.Write.Roles
    @PutMapping("/{roleID}/local-default")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse markRoleAsLocalDefault(@PathVariable UUID roleID) {
        return roleService.markAsLocalDefault(roleID);
    }

    /**
     * Marks the given role as default for externally registered users.
     *
     * @param roleID role ID
     * @return role details
     */
    @Permit.Write.Roles
    @PutMapping("/{roleID}/external-default")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse markRoleAsExternalDefault(@PathVariable UUID roleID) {
        return roleService.markAsExternalDefault(roleID);
    }

    /**
     * Assigns the given permission to the given role.
     *
     * @param roleID role ID
     * @param permissionID ID of the permission to assign
     * @return role details
     */
    @Permit.Write.Roles
    @PutMapping("/{roleID}/permissions/{permissionID}")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse assignPermission(@PathVariable UUID roleID, @PathVariable UUID permissionID) {
        return roleService.assignPermission(roleID, permissionID);
    }

    /**
     * Unassigns the given permission from the given role.
     *
     * @param roleID role ID
     * @param permissionID ID of the permission to unassign
     * @return role details
     */
    @Permit.Write.Roles
    @DeleteMapping("/{roleID}/permissions/{permissionID}")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleResponse unassignPermission(@PathVariable UUID roleID, @PathVariable UUID permissionID) {
        return roleService.unassignPermission(roleID, permissionID);
    }

    /**
     * Removes an existing role.
     *
     * @param roleID role ID
     */
    @Permit.Write.Roles
    @DeleteMapping("/{roleID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable UUID roleID) {
        roleService.deleteRole(roleID);
    }
}
