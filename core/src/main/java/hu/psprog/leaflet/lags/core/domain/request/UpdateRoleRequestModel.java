package hu.psprog.leaflet.lags.core.domain.request;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * Request model for updating user role.
 *
 * @author Peter Smith
 */
@Data
public class UpdateRoleRequestModel implements Serializable {

    @NotNull
    private LegacyRole role;

}
