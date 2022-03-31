package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Service layer for authentication related operations.
 *
 * @author Peter Smith
 */
public interface AuthenticationService {

    /**
     * Processes a sign-up request.
     *
     * @param signUpRequestModel {@link SignUpRequestModel} object containing data of the user to be registered
     * @param request {@link HttpServletRequest} object to gather additional request information (like redirection URI)
     * @return result of sign-up request processing as {@link SignUpStatus}
     */
    SignUpResult signUp(SignUpRequestModel signUpRequestModel, HttpServletRequest request);
}
