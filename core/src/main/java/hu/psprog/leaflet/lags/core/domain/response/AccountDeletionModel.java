package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;
import lombok.Data;

/**
 * Request model for account deletion operation.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class AccountDeletionModel {

    private String currentPassword;
}
