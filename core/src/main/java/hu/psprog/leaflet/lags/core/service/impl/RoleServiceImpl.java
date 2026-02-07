package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;
import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.RoleMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.persistence.dao.RoleDAO;
import hu.psprog.leaflet.lags.core.service.RoleService;
import hu.psprog.leaflet.lags.core.service.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link RoleService}.
 *
 * @author Peter Smith
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleDAO roleDAO;
    private final PermissionDAO permissionDAO;
    private final RoleMapper roleMapper;

    @Autowired
    public RoleServiceImpl(RoleDAO roleDAO, PermissionDAO permissionDAO, RoleMapper roleMapper) {
        this.roleDAO = roleDAO;
        this.permissionDAO = permissionDAO;
        this.roleMapper = roleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getRoles(int page) {

        return roleDAO.findAll(PaginationUtil.createPageRequest(page))
                .map(roleMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRole(UUID roleID) {
        return findRequiredRole(roleID, roleMapper::map);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest role) {

        Role newRole = roleMapper.map(role);
        Role savedRole = exceptionAwareCall(() -> roleDAO.save(newRole));

        log.info("Role '{}' created with ID={}", newRole.getName(), savedRole.getId());

        return roleMapper.map(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse editRole(UUID roleID, RoleRequest role) {

        Role currentRole = findRequiredRole(roleID, Function.identity());
        currentRole.setName(role.name());
        currentRole.setDescription(role.description());
        Role savedRole = exceptionAwareCall(() -> roleDAO.save(currentRole));

        log.info("Role '{}' updated with ID={}", currentRole.getName(), savedRole.getId());

        return roleMapper.map(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRoleStatus(UUID roleID, boolean enabled) {

        Role currentRoleData = findRequiredRole(roleID, Function.identity());
        currentRoleData.setEnabled(enabled);

        exceptionAwareCall(() -> roleDAO.save(currentRoleData));

        log.info("Status of role {} ({}) updated successfully to enabled={}", currentRoleData.getName(), roleID, enabled);

        return roleMapper.map(currentRoleData);
    }

    @Override
    @Transactional
    public RoleResponse markAsLocalDefault(UUID roleID) {

        Role currentRoleData = findRequiredRole(roleID, Function.identity());
        roleDAO.removeCurrentLocalDefaultFlag();
        currentRoleData.setLocalDefault(true);
        exceptionAwareCall(() -> roleDAO.save(currentRoleData));

        log.info("Marked role {} ({}) as default for locally registered users", currentRoleData.getName(), roleID);

        return roleMapper.map(currentRoleData);
    }

    @Override
    @Transactional
    public RoleResponse markAsExternalDefault(UUID roleID) {

        Role currentRoleData = findRequiredRole(roleID, Function.identity());
        roleDAO.removeCurrentExternalDefaultFlag();
        currentRoleData.setExternalDefault(true);
        exceptionAwareCall(() -> roleDAO.save(currentRoleData));

        log.info("Marked role {} ({}) as default for externally registered users", currentRoleData.getName(), roleID);

        return roleMapper.map(currentRoleData);
    }

    @Override
    public RoleResponse assignPermission(UUID roleID, UUID permissionID) {
        return doAssignment(roleID, permissionID, true);
    }

    @Override
    public RoleResponse unassignPermission(UUID roleID, UUID permissionID) {
        return doAssignment(roleID, permissionID, false);
    }

    @Override
    public void deleteRole(UUID roleID) {

        exceptionAwareCall(() -> roleDAO.delete(roleID));

        log.info("Role {} deleted successfully", roleID);
    }

    private <T> T findRequiredRole(UUID roleID, Function<Role, T> mapperFunction) {

        return roleDAO.findByID(roleID)
                .map(mapperFunction)
                .orElseThrow(() -> {
                    log.error("Role by ID={} not found", roleID);
                    return ResourceNotFoundException.role(roleID);
                });
    }

    private <T> T exceptionAwareCall(Supplier<T> call) {

        try {
            return call.get();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting role: {}", exception.getMessage(), exception);
            throw ConflictingResourceException.onCreate(ManagedResourceType.ROLE);
        }
    }

    private void exceptionAwareCall(Runnable call) {

        try {
            call.run();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting role: {}", exception.getMessage(), exception);
            throw ConflictingResourceException.onDelete(ManagedResourceType.ROLE);
        }
    }

    private RoleResponse doAssignment(UUID roleID, UUID permissionID, boolean toAssign) {

        Role currentRoleData = findRequiredRole(roleID, Function.identity());

        boolean isAssigned = currentRoleData.getPermissions()
                .stream()
                .map(Permission::getId)
                .anyMatch(permissionID::equals);

        if (toAssign == isAssigned) {
            log.warn("Permission {} is already {} role {} ({})", permissionID,
                    toAssign ? "assigned to" : "unassigned from", currentRoleData.getName(), roleID);
            return roleMapper.map(currentRoleData);
        }

        Permission permissionToBeAssigned = permissionDAO.findByID(permissionID)
                .orElseThrow(() -> {
                    log.error("Permission by ID={} not found", permissionID);
                    return ResourceNotFoundException.permission(permissionID);
                });

        if (toAssign) {
            currentRoleData.getPermissions().add(permissionToBeAssigned);
        } else {
            currentRoleData.getPermissions().remove(permissionToBeAssigned);
        }

        exceptionAwareCall(() -> roleDAO.save(currentRoleData));

        log.info("Permission {} ({}) has been {} role {} ({})", permissionToBeAssigned.getName(), permissionID,
                toAssign ? "assigned to" : "unassigned from", currentRoleData.getName(), roleID);

        return findRequiredRole(roleID, roleMapper::map);
    }
}
