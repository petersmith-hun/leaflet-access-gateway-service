package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA repository interface for {@link Role} entity operations.
 *
 * @author Peter Smith
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Disables the "local default" flag of the entity currently having that enabled.
     */
    @Modifying
    @Query("update Role role set role.localDefault = false where role.localDefault is true")
    void removeCurrentLocalDefaultFlag();

    /**
     * Disables the "external default" flag of the entity currently having that enabled.
     */
    @Modifying
    @Query("update Role role set role.externalDefault = false where role.externalDefault is true")
    void removeCurrentExternalDefaultFlag();
}
