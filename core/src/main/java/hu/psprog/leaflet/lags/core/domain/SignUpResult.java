package hu.psprog.leaflet.lags.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Domain class representing the result of a sign-up request.
 *
 * @author Peter Smith
 */
@Data
@AllArgsConstructor
public class SignUpResult {

    private final String redirectURI;
    private final SignUpStatus signUpStatus;
}
