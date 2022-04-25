package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Domain object holding information for successful password reset notification mail.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class PasswordResetSuccess {

    private String participant;
    private String username;
}
