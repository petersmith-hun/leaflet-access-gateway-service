package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

/**
 * {@link AccountRequestHandler} implementation, that handles the sign-up/-in process of an external user account.
 *
 * The implementation does the following steps:
 *  1) Checks whether a user account already exists with the given email address.
 *  2) If so, verifies that the stored account type matches the one in the user definition. If that's the case, it means
 *     that the account is already mirrored into the local user database and the sign-up process can be considered
 *     successful. Otherwise, an account with the same email address exists from a different source, so the sign-up
 *     process cannot be finished (indicated with {@link SignUpStatus#ADDRESS_IN_USE} status).
 *  3) If no account exists with the given email address, sign-up process can move ahead with creating the new account
 *     information and persisting it in the local user database as a mirror of the external account.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class ExternalAccountSignUpAccountRequestHandler implements AccountRequestHandler<ExternalUserDefinition<?>, SignUpStatus> {

    private final UserDAO userDAO;
    private final ConversionService conversionService;

    @Autowired
    public ExternalAccountSignUpAccountRequestHandler(UserDAO userDAO, ConversionService conversionService) {
        this.userDAO = userDAO;
        this.conversionService = conversionService;
    }

    @Override
    public SignUpStatus processAccountRequest(ExternalUserDefinition<?> accountRequest) {

        return userDAO.findByEmail(accountRequest.getEmail())
                .map(user -> verifyExistingExternalUser(accountRequest, user))
                .orElseGet(() -> saveNewExternalUser(accountRequest));
    }

    private SignUpStatus verifyExistingExternalUser(ExternalUserDefinition<?> accountRequest, User user) {

        SignUpStatus signUpStatus = SignUpStatus.SUCCESS;
        if (user.getAccountType() != accountRequest.getAccountType()) {
            log.warn("A user account of type [{}] already exists for email address [{}] from external provider [{}]",
                    user.getAccountType(), user.getEmail(), accountRequest.getAccountType());
            signUpStatus = SignUpStatus.ADDRESS_IN_USE;
        }

        return signUpStatus;
    }

    private SignUpStatus saveNewExternalUser(ExternalUserDefinition<?> accountRequest) {

        SignUpStatus signUpStatus = SignUpStatus.SUCCESS;
        try {
            User externalUser = conversionService.convert(accountRequest, User.class);
            userDAO.save(externalUser);
        } catch (Exception exception) {
            log.error("Failed to save external user account [{}|email={}]",
                    accountRequest.getAccountType(), accountRequest.getEmail(), exception);
            signUpStatus = SignUpStatus.FAILURE;
        }

        return signUpStatus;
    }
}
