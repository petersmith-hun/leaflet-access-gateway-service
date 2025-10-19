package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public void saveAll(Iterable<Permission> permissions) {
        permissionRepository.saveAll(permissions);
    }

    @Override
    public List<Permission> findAllByNameIn(List<String> names) {
        return permissionRepository.findAllByNameIn(names);
    }
}
