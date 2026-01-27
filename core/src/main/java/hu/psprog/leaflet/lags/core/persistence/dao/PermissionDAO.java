package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO interface for permission objects.
 *
 * @author Peter Smith
 */
@Component
public interface PermissionDAO {

    /**
     * Retrieves all {@link Permission} entities by the given list of names.
     *
     * @param names name of each permission to be retrieved
     * @return identified {@link Permission} entities
     */
    List<Permission> findAllByNames(List<String> names);

    /**
     * Retrieves a page of existing {@link Permission} entities.
     *
     * @param pageable page settings
     * @return page of {@link Permission} entities
     */
    Page<Permission> findAll(Pageable pageable);

    /**
     * Retrieves a {@link Permission} record by its ID.
     *
     * @param id ID of the permission
     * @return identified {@link Permission} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<Permission> findByID(UUID id);

    /**
     * Stores (creates or updates) the given {@link Permission} entity.
     *
     * @param permission entity data to be stored
     * @return saved entity instance
     */
    Permission save(Permission permission);

    /**
     * Stores (creates or updates) the given collection of permissions.
     *
     * @param permissions collection of {@link Permission} entities to be stored
     */
    void saveAll(Iterable<Permission> permissions);

    /**
     * Removes the given permission.
     *
     * @param id ID of the permission to be deleted
     */
    void delete(UUID id);
}
