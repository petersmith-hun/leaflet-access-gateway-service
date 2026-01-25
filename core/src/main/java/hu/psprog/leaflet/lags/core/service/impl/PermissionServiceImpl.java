package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingPermissionException;
import hu.psprog.leaflet.lags.core.exception.PermissionNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.PermissionMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.service.PermissionService;
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
 * Default implementation of {@link PermissionService}.
 *
 * @author Peter Smith
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionDAO permissionDAO;
    private final PermissionMapper permissionMapper;

    @Autowired
    public PermissionServiceImpl(PermissionDAO permissionDAO, PermissionMapper permissionMapper) {
        this.permissionDAO = permissionDAO;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponse> getPermissions(int page) {

        return permissionDAO.findAll(PaginationUtil.createPageRequest(page))
                .map(permissionMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermission(UUID permissionID) {
        return findRequiredPermission(permissionID, permissionMapper::map);
    }

    @Override
    @Transactional
    public PermissionResponse createPermission(PermissionRequest permission) {

        Permission newPermission = permissionMapper.map(permission);
        Permission savedPermission = exceptionAwareCall(() -> permissionDAO.save(newPermission));

        log.info("Permission '{}' created with ID={}", newPermission.getName(), savedPermission.getId());

        return permissionMapper.map(savedPermission);
    }

    @Override
    @Transactional
    public PermissionResponse editPermission(UUID permissionID, PermissionRequest permission) {

        Permission currentPermission = findRequiredPermission(permissionID, Function.identity());
        currentPermission.setName(permission.name());
        currentPermission.setDescription(permission.description());
        Permission savedPermission = exceptionAwareCall(() -> permissionDAO.save(currentPermission));

        log.info("Permission '{}' updated with ID={}", currentPermission.getName(), savedPermission.getId());

        return permissionMapper.map(savedPermission);
    }

    @Override
    @Transactional
    public PermissionResponse updatePermissionStatus(UUID permissionID, boolean enabled) {

        Permission currentPermissionData = findRequiredPermission(permissionID, Function.identity());
        currentPermissionData.setEnabled(enabled);

        exceptionAwareCall(() -> permissionDAO.save(currentPermissionData));

        log.info("Status of permission {} ({}) updated successfully to enabled={}", currentPermissionData.getName(), permissionID, enabled);

        return permissionMapper.map(currentPermissionData);
    }

    @Override
    public void deletePermission(UUID permissionID) {

        exceptionAwareCall(() -> permissionDAO.delete(permissionID));

        log.info("Permission {} deleted successfully", permissionID);
    }

    private <T> T findRequiredPermission(UUID permissionsID, Function<Permission, T> mapperFunction) {

        return permissionDAO.findByID(permissionsID)
                .map(mapperFunction)
                .orElseThrow(() -> {
                    log.error("Permission by ID={} not found", permissionsID);
                    return new PermissionNotFoundException(permissionsID);
                });
    }

    private <T> T exceptionAwareCall(Supplier<T> call) {

        try {
            return call.get();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting permission: {}", exception.getMessage(), exception);
            throw ConflictingPermissionException.onCreate();
        }
    }

    private void exceptionAwareCall(Runnable call) {

        try {
            call.run();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting permission: {}", exception.getMessage(), exception);
            throw ConflictingPermissionException.onDelete();
        }
    }
}
