package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.persistence.dao.RoleDAO;
import hu.psprog.leaflet.lags.core.persistence.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Default implementation of {@link RoleDAO}.
 *
 * @author Peter Smith
 */
@Component
public class RoleDAOImpl implements RoleDAO {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleDAOImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public Optional<Role> findByID(UUID id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void delete(UUID id) {
        roleRepository.deleteById(id);
    }

    @Override
    public void removeCurrentLocalDefaultFlag() {
        roleRepository.removeCurrentLocalDefaultFlag();
    }

    @Override
    public void removeCurrentExternalDefaultFlag() {
        roleRepository.removeCurrentExternalDefaultFlag();
    }
}
