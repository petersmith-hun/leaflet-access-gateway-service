package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * DAO interface for role objects.
 *
 * @author Peter Smith
 */
public interface RoleDAO {

    /**
     * Retrieves a page of existing {@link Role} entities.
     *
     * @param pageable page settings
     * @return page of {@link Role} entities
     */
    Page<Role> findAll(Pageable pageable);

    /**
     * Retrieves a {@link Role} record by its ID.
     *
     * @param id ID of the role
     * @return identified {@link Role} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<Role> findByID(UUID id);

    /**
     * Stores (creates or updates) the given {@link Role} entity.
     *
     * @param role entity data to be stored
     * @return saved entity instance
     */
    Role save(Role role);

    /**
     * Removes the given role.
     *
     * @param id ID of the role to be deleted
     */
    void delete(UUID id);

    /**
     * Disables the "local default" flag of the entity currently having that enabled.
     */
    void removeCurrentLocalDefaultFlag();

    /**
     * Disables the "external default" flag of the entity currently having that enabled.
     */
    void removeCurrentExternalDefaultFlag();
}
