package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;
import hu.psprog.leaflet.lags.core.domain.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * {@link AccountRequestHandler} implementation processing sign-up requests.
 *
 * The implementation does the following steps:
 *  - Converts the sign-up request into a user account with safe defaults.
 *  - Stores the user record.
 *  - Sends an email notification to the user about the successful sign-up.
 *
 * Processing of the request is stopped in case the specified email address is already in use, or any other unexpected
 * error happens while processing the sign-up request.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class SignUpAccountRequestHandler implements AccountRequestHandler<SignUpRequestModel, SignUpResult> {

    private final UserDAO userDAO;
    private final ConversionService conversionService;
    private final NotificationAdapter notificationAdapter;

    @Autowired
    public SignUpAccountRequestHandler(UserDAO userDAO, ConversionService conversionService, NotificationAdapter notificationAdapter) {
        this.userDAO = userDAO;
        this.conversionService = conversionService;
        this.notificationAdapter = notificationAdapter;
    }

    @Override
    public SignUpResult processAccountRequest(SignUpRequestModel signUpRequestModel) {

        User user = conversionService.convert(signUpRequestModel, User.class);
        SignUpStatus status;

        try {
            userDAO.save(user);
            status = SignUpStatus.SUCCESS;
            log.info("User account successfully created with userID=[{}].", user.getId());

            sendConfirmationMail(signUpRequestModel);

        } catch (DataIntegrityViolationException exception) {
            status = SignUpStatus.ADDRESS_IN_USE;
            log.error("User account creation failed with data integrity violation - is email address already in use?", exception);

        } catch (Exception exception) {
            status = SignUpStatus.FAILURE;
            log.error("User account creation failed with an unknown reason", exception);
        }

        return SignUpResult.createByStatus(status);
    }

    private void sendConfirmationMail(SignUpRequestModel signUpRequestModel) {

        notificationAdapter.signUpConfirmation(SignUpConfirmation.builder()
                .username(signUpRequestModel.getUsername())
                .email(signUpRequestModel.getEmail())
                .build());
    }
}
