package hu.psprog.leaflet.lags.core.service.util;

import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;

import java.util.Optional;

/**
 * Token tracking related operations.
 * Token tracking lets LAGS follow issued tokens by storing their meta-information. This way clients can be instructed
 * to frequently (e.g. on every request) have the tokens rechecked should they have been revoked (by sign-out).
 *
 * @author Peter Smith
 */
public interface TokenTracker {

    /**
     * Stores a token meta-information object.
     * Input object can only contain some basic information about the token, such as token ID, the expiration, etc.
     * Actually stored meta-information object will contain some extra information, with the properly initialized defaults.
     *
     * @param storeAccessTokenInfoRequest {@link StoreAccessTokenInfoRequest} object containing the basic token info pieces
     */
    void storeTokenInfo(StoreAccessTokenInfoRequest storeAccessTokenInfoRequest);

    /**
     * Retrieves an already token meta-information record by the token's ID.
     *
     * @param jti token ID
     * @return token meta-information object as {@link AccessTokenInfo} wrapped in {@link Optional} or empty {@link Optional} if missing
     */
    Optional<AccessTokenInfo> retrieveTokenInfo(String jti);

    /**
     * Sets the identified token's status flag to revoked.
     *
     * @param jti token ID
     */
    void revokeToken(String jti);

    /**
     * Iterates over the expired stored tokens and deletes them from the token tracking repository.
     */
    void cleanUpExpiredToken();
}
