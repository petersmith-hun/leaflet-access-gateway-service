package hu.psprog.leaflet.lags.core.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request model for updating user role.
 *
 * @author Peter Smith
 */
@Data
public class UpdateRoleRequestModel implements Serializable {

    @NotNull
    private UUID roleID;

}
