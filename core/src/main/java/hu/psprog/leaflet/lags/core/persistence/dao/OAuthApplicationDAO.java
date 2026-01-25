package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO interface for OAuth application registration objects.
 *
 * @author Peter Smith
 */
public interface OAuthApplicationDAO {

    /**
     * Stores (creates or updates) the given {@link OAuthApplication} entity.
     *
     * @param entity entity data to be stored
     * @return saved entity instance
     */
    OAuthApplication save(OAuthApplication entity);

    /**
     * Retrieves a page of existing {@link OAuthApplication} definitions.
     *
     * @param pageable page settings
     * @return page of {@link OAuthApplication} entities
     */
    Page<OAuthApplication> findAll(Pageable pageable);

    /**
     * Retrieves an {@link OAuthApplication} record by its ID.
     *
     * @param applicationID ID of the registration
     * @return identified {@link OAuthApplication} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<OAuthApplication> findByID(UUID applicationID);

    /**
     * Retrieves an {@link OAuthApplication} record by its name.
     *
     * @param name name of the registration
     * @return identified {@link OAuthApplication} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<OAuthApplication> findByName(String name);

    /**
     * Retrieves an {@link OAuthApplication} record by its assigned client ID.
     *
     * @param clientID OAuth client ID of the registration
     * @return identified {@link OAuthApplication} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<OAuthApplication> findByClientID(String clientID);

    /**
     * Retrieves an {@link OAuthApplication} record by its assigned audience.
     *
     * @param audience OAuth audience of the registration
     * @return identified {@link OAuthApplication} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<OAuthApplication> findByAudience(String audience);

    /**
     * Retrieves the related resource server application definitions for the given target application.
     *
     * @param targetApplicationID ID of the application to retrieve resource servers of
     * @return list of related {@link OAuthApplication} entities
     */
    List<OAuthApplication> findResourceServersForTargetApplication(UUID targetApplicationID);

    /**
     * Returns the number of existing {@link OAuthApplication} records.
     *
     * @return number of existing {@link OAuthApplication} records
     */
    long count();

    /**
     * Removes the given OAuth application definition.
     *
     * @param applicationID ID of the application to be deleted
     */
    void delete(UUID applicationID);
}
