package hu.psprog.leaflet.lags.core.service.mailing.domain;

import lombok.Data;

/**
 * Mail domain for sign-up confirmation mails.
 *
 * @author Peter Smith
 */
@Data
public class SignUpConfirmation {

    private final String username;
    private final String email;
}
