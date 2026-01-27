package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of {@link PermissionDAO}.
 *
 * @author Peter Smith
 */
@Component
public class PermissionDAOImpl implements PermissionDAO {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionDAOImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> findAllByNames(List<String> names) {
        return permissionRepository.findAllByNameIn(names);
    }

    @Override
    public Page<Permission> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Override
    public Optional<Permission> findByID(UUID id) {
        return permissionRepository.findById(id);
    }

    @Override
    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public void saveAll(Iterable<Permission> permissions) {
        permissionRepository.saveAll(permissions);
    }

    @Override
    public void delete(UUID id) {
        permissionRepository.deleteById(id);
    }
}
