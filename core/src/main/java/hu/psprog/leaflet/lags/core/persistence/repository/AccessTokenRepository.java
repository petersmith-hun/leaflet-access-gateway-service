package hu.psprog.leaflet.lags.core.persistence.repository;

import hu.psprog.leaflet.lags.core.domain.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.TokenStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository managing followed OAuth2 access tokens.
 *
 * @author Peter Smith
 */
public interface AccessTokenRepository {

    /**
     * Stores the given {@link AccessTokenInfo} object into the tracking repository.
     *
     * @param accessTokenInfo {@link AccessTokenInfo} object to be stored
     */
    void save(AccessTokenInfo accessTokenInfo);

    /**
     * Retrieves a stored {@link AccessTokenInfo} record by the given token ID (JTI).
     *
     * @param jti token ID (JTI value of the token)
     * @return the stored {@link AccessTokenInfo} record wrapped as {@link Optional} or empty {@link Optional} is not present
     */
    Optional<AccessTokenInfo> retrieveByJTI(String jti);

    /**
     * Retrieves all stored access token meta-information records.
     *
     * @return list of {@link AccessTokenInfo} objects
     */
    List<AccessTokenInfo> getAllAccessTokenInfo();

    /**
     * Deletes a stored {@link AccessTokenInfo} record by the given token ID (JTI).
     *
     * @param jti token ID (JTI value of the token)
     */
    void deleteByJTI(String jti);

    /**
     * Updates the status of a stored {@link AccessTokenInfo} record by the given token ID (JTI) to the given one.
     *
     * @param jti token ID (JTI value of the token)
     * @param newStatus the new status as {@link TokenStatus}
     */
    void updateStatusByJTI(String jti, TokenStatus newStatus);
}
