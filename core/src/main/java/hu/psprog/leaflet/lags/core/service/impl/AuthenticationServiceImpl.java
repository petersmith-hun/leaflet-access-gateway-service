package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.domain.SignUpStatus;
import hu.psprog.leaflet.lags.core.domain.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.AuthenticationService;
import hu.psprog.leaflet.lags.core.service.mailing.domain.SignUpConfirmation;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.util.ReCaptchaValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of {@link AuthenticationService}.
 *
 * @author Peter Smith
 */
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDAO userDAO;
    private final ConversionService conversionService;
    private final ReCaptchaValidator reCaptchaValidator;
    private final NotificationAdapter notificationAdapter;

    @Autowired
    public AuthenticationServiceImpl(UserDAO userDAO, ConversionService conversionService,
                                     ReCaptchaValidator reCaptchaValidator, NotificationAdapter notificationAdapter) {
        this.userDAO = userDAO;
        this.conversionService = conversionService;
        this.reCaptchaValidator = reCaptchaValidator;
        this.notificationAdapter = notificationAdapter;
    }

    @Override
    public SignUpResult signUp(SignUpRequestModel signUpRequestModel, HttpServletRequest request) {

        String redirectURI = request.getParameter(OAuthConstants.Request.REDIRECT_URI);

        return new SignUpResult(redirectURI, reCaptchaValidator.isValid(signUpRequestModel, request)
                ? processSignUp(signUpRequestModel)
                : SignUpStatus.RE_CAPTCHA_VERIFICATION_FAILED);
    }

    private SignUpStatus processSignUp(SignUpRequestModel signUpRequestModel) {

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

        return status;
    }

    private void sendConfirmationMail(SignUpRequestModel signUpRequestModel) {

        SignUpConfirmation signUpConfirmation = new SignUpConfirmation(signUpRequestModel.getUsername(), signUpRequestModel.getEmail());
        notificationAdapter.signUpConfirmation(signUpConfirmation);
    }
}
