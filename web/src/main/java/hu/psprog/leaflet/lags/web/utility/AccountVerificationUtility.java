package hu.psprog.leaflet.lags.web.utility;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.UserProfileService;
import hu.psprog.leaflet.lags.web.exception.NonLocalAccountEditAttemptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Utility implementation to verify if the currently active account is local.
 *
 * @author Peter Smith
 */
@Component
public class AccountVerificationUtility {

    private final UserProfileService userProfileService;

    @Autowired
    public AccountVerificationUtility(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Asserts that the active user has a local account, throws exception otherwise.
     *
     * @param authentication {@link Authentication} object to extract the active user's ID and resolve it
     * @throws NonLocalAccountEditAttemptException if the active user has an external account
     */
    public void assertLocalAccount(Authentication authentication) {

        if (isLocalAccount(authentication)) {
            return;
        }

        throw new NonLocalAccountEditAttemptException();
    }

    /**
     * Checks if the active user has a local account, responds with a boolean flag.
     *
     * @param authentication {@link Authentication} object to extract the active user's ID and resolve it
     * @return boolean flag indicating whether the user has a local account
     */
    public boolean isLocalAccount(Authentication authentication) {
        return isLocalAccount(userProfileService.getUserDetails(authentication));
    }

    /**
     * Checks if the active user has a local account, responds with a boolean flag.
     *
     * @param userDetailsResponse {@link UserDetailsResponse} object containing the already resolved user data
     * @return boolean flag indicating whether the user has a local account
     */
    public boolean isLocalAccount(UserDetailsResponse userDetailsResponse) {
        return userDetailsResponse.accountType() == AccountType.LOCAL;
    }
}
