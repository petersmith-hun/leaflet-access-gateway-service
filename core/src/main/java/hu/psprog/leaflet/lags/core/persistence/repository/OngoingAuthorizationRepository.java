package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.OngoingAuthorization;

import java.util.Optional;

/**
 * Repository managing the "ongoing authorization" records.
 *
 * @author Peter Smith
 */
public interface OngoingAuthorizationRepository {

    /**
     * Retrieves a stored {@link OngoingAuthorization} object by its assigned authorization code.
     *
     * @param authorizationCode authorization code of the in-progress authorization
     * @return populated {@link Optional} with {@link OngoingAuthorization} object if found, empty {@link Optional} otherwise
     */
    Optional<OngoingAuthorization> getOngoingAuthorizationByCode(String authorizationCode);

    /**
     * Saves a new {@link OngoingAuthorization} object.
     * Records in the storage will be identified by the provided authorization code value.
     *
     * @param ongoingAuthorization {@link OngoingAuthorization} object to be stored
     */
    void saveOngoingAuthorization(OngoingAuthorization ongoingAuthorization);

    /**
     * Deletes a stored {@link OngoingAuthorization} object by its assigned authorization code.
     *
     * @param authorizationCode authorization code of the in-progress authorization
     */
    void deleteOngoingAuthorization(String authorizationCode);
}
