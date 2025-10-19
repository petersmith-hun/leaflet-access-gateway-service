package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA repository interface for {@link Permission} entity operations.
 *
 * @author Peter Smith
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Retrieves all {@link Permission} entities by the given list of names.
     *
     * @param names name of each permission to be retrieved
     * @return identified {@link Permission} entities
     */
    List<Permission> findAllByNameIn(List<String> names);
}
