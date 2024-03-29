package hu.psprog.leaflet.lags.core.domain.notification;

import lombok.Builder;
import lombok.Data;

/**
 * Mail domain for sign-up confirmation mails.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class SignUpConfirmation {

    private final String username;
    private final String email;
}
