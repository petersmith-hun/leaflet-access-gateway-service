package hu.psprog.leaflet.lags.core.domain.notification;

import lombok.Builder;
import lombok.Data;

/**
 * Domain object holding password reset request information for reset mail.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class PasswordResetRequest {

    private String recipient;
    private String username;
    private String token;
    private String resetLink;
    private int expiration;
}
