package hu.psprog.leaflet.lags.core.persistence.dao;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;

import java.util.Optional;

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
     * Returns the number of existing {@link OAuthApplication} records.
     *
     * @return number of existing {@link OAuthApplication} records
     */
    long count();
}
