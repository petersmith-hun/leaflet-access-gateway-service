package hu.psprog.leaflet.lags.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_SIGNUP;

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

    /**
     * Creates a {@link SignUpResult} objects based on the specified {@link SignUpStatus}.
     * Successful status will cause the login endpoint to be added as redirection URI, otherwise the sign-up
     * endpoint will be set.
     *
     * @param status {@link SignUpStatus} value
     * @return created {@link SignUpResult} object
     */
    public static SignUpResult createByStatus(SignUpStatus status) {
        return new SignUpResult(getRedirectURI(status), status);
    }

    private static String getRedirectURI(SignUpStatus status) {

        return SignUpStatus.SUCCESS == status
                ? PATH_LOGIN
                : PATH_SIGNUP;
    }
}
