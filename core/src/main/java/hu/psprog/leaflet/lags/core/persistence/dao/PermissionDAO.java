package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DAO interface for permission objects.
 *
 * @author Peter Smith
 */
@Component
public interface PermissionDAO {

    /**
     * Stores (creates or updates) the given collection of permissions.
     *
     * @param permissions collection of {@link Permission} entities to be stored
     */
    void saveAll(Iterable<Permission> permissions);

    /**
     * Retrieves all {@link Permission} entities by the given list of names.
     *
     * @param names name of each permission to be retrieved
     * @return identified {@link Permission} entities
     */
    List<Permission> findAllByNameIn(List<String> names);
}
