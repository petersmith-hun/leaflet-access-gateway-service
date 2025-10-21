package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository interface for {@link OAuthApplication} operations.
 *
 * @author Peter Smith
 */
@Repository
public interface OAuthApplicationRepository extends JpaRepository<OAuthApplication, Long> {

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
    Optional<OAuthApplication> findByClientId(String clientID);

    /**
     * Retrieves an {@link OAuthApplication} record by its assigned audience.
     *
     * @param audience OAuth audience of the registration
     * @return identified {@link OAuthApplication} record wrapped as {@link Optional} or empty {@link Optional} if none found
     */
    Optional<OAuthApplication> findByAudience(String audience);
}
