package hu.psprog.leaflet.lags.core.domain.internal;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import lombok.Builder;
import lombok.Data;

/**
 * Internal domain class for wrapping external user account data.
 *
 * @param <ID> type of the primary ID of the user
 * @author Peter Smith
 */
@Data
@Builder
public class ExternalUserDefinition<ID> {

    private final ID userID;
    private final String email;
    private final String username;
    private final LegacyRole role = LegacyRole.EXTERNAL_USER;
    private final AccountType accountType;
}
