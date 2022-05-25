package hu.psprog.leaflet.lags.core.domain.internal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Domain class representing a followed access token.
 *
 * @author Peter Smith
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AccessTokenInfo extends StoreAccessTokenInfoRequest {

    private TokenStatus status;
    private Date revokedAt;

    /**
     * Wraps a {@link StoreAccessTokenInfoRequest} into an {@link AccessTokenInfo} object settings its status to be
     * active, and its revocation date to be null (since it's not yet revoked).
     * Can be used to prepare an access token meta-information object for storing it.
     *
     * @param request {@link StoreAccessTokenInfoRequest} to be wrapped
     */
    public AccessTokenInfo(StoreAccessTokenInfoRequest request) {
        super(request.getId(), request.getSubject(), request.getIssuedAt(), request.getExpiresAt());
        this.status = TokenStatus.ACTIVE;
        this.revokedAt = null;
    }
}
