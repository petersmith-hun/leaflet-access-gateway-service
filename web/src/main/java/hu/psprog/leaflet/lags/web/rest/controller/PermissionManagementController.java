package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import hu.psprog.leaflet.lags.core.service.PermissionService;
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
 * REST controller endpoints for permission operations.
 *
 * @author Peter Smith
 */
@Slf4j
@RestController
@RequestMapping("/access-management/permissions")
public class PermissionManagementController extends BaseManagementController {

    private final PermissionService permissionService;

    @Autowired
    public PermissionManagementController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Retrieves the given page of permissions.
     *
     * @param page 1-based page number, defaults to 0 (turns off pagination)
     * @return permission details
     */
    @Permit.Read.Permissions
    @GetMapping
    public Page<PermissionResponse> getPermissions(@RequestParam(value = "page", defaultValue = "0") int page) {
        return permissionService.getPermissions(page);
    }

    /**
     * Retrieves the details of the given permission.
     *
     * @param permissionID permission ID
     * @return permission details
     */
    @Permit.Read.Permissions
    @GetMapping("/{permissionID}")
    public PermissionResponse getPermission(@PathVariable UUID permissionID) {
        return permissionService.getPermission(permissionID);
    }

    /**
     * Creates a new permission.
     *
     * @param request permission data
     * @return permission details
     */
    @Permit.Write.Permissions
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse createPermission(@Valid @RequestBody PermissionRequest request) {
        return permissionService.createPermission(request);
    }

    /**
     * Edits an existing permission.
     *
     * @param permissionID permission ID
     * @param request permission data
     * @return permission details
     */
    @Permit.Write.Permissions
    @PutMapping("/{permissionID}")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse editPermission(@PathVariable UUID permissionID, @Valid @RequestBody PermissionRequest request) {
        return permissionService.editPermission(permissionID, request);
    }

    /**
     * Enables the given permission.
     *
     * @param permissionID permission ID
     * @return permission details
     */
    @Permit.Write.Permissions
    @PutMapping("/{permissionID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse enablePermission(@PathVariable UUID permissionID) {
        return permissionService.updatePermissionStatus(permissionID, true);
    }

    /**
     * Disables the given permission.
     *
     * @param permissionID permission ID
     * @return permission details
     */
    @Permit.Write.Permissions
    @DeleteMapping("/{permissionID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse disablePermission(@PathVariable UUID permissionID) {
        return permissionService.updatePermissionStatus(permissionID, false);
    }

    /**
     * Removes an existing permission.
     *
     * @param permissionID permission ID
     */
    @Permit.Write.Permissions
    @DeleteMapping("/{permissionID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePermission(@PathVariable UUID permissionID) {
        permissionService.deletePermission(permissionID);
    }
}
