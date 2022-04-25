package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.domain.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.SignUpResult;
import hu.psprog.leaflet.lags.core.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_LOGIN;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_PASSWORD_RESET;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_PASSWORD_RESET_CONFIRMATION;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_SIGNUP;
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.QUERY_PARAMETER_TOKEN;

/**
 * Controller for authentication related operations.
 *
 * @author Peter Smith
 */
@Controller
public class AuthenticationController {

    private static final String VIEW_LOGIN = "views/login";
    private static final String VIEW_SIGNUP = "views/signup";
    private static final String VIEW_PW_RESET_REQUEST = "views/pw_reset_request";
    private static final String VIEW_PW_RESET_CONFIRM = "views/pw_reset_confirm";
    private static final String VIEW_PW_RESET_ACK = "views/pw_reset_ack";
    private static final String VIEW_REDIRECT_LOGIN = "redirect:/login?pwreset_status=success";
    private static final String VIEW_REDIRECT_SIGNUP_TEMPLATE = "redirect:%s?signup_status=%s";
    private static final String ATTRIBUTE_RECAPTCHA_SITE_KEY = "recaptchaSiteKey";
    private static final String ATTRIBUTE_VALIDATION_ERROR = "validationError";

    private final AuthenticationService authenticationService;
    private final String recaptchaSiteKey;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AuthenticationConfig authenticationConfig) {
        this.authenticationService = authenticationService;
        recaptchaSiteKey = authenticationConfig.getRecaptchaSiteKey();
    }

    /**
     * GET /login
     * Renders the login form.
     *
     * @return populated {@link ModelAndView} object
     */
    @GetMapping(PATH_LOGIN)
    public ModelAndView renderLoginForm() {
        return new ModelAndView(VIEW_LOGIN);
    }

    /**
     * GET /signup
     * Renders sign-up form.
     *
     * @param signUpRequestModel {@link SignUpRequestModel} object in order to re-populate the form after a validation error
     * @return populated {@link ModelAndView} object
     */
    @GetMapping(PATH_SIGNUP)
    public ModelAndView renderSignUpForm(@ModelAttribute SignUpRequestModel signUpRequestModel) {

        return new ModelAndView(VIEW_SIGNUP, Map.of(
                ATTRIBUTE_RECAPTCHA_SITE_KEY, recaptchaSiteKey,
                ATTRIBUTE_VALIDATION_ERROR, signUpRequestModel.isValidationFailed()
        ));
    }

    /**
     * POST /signup
     * Processes the given sign-up request. Renders sign-up form with current {@link SignUpRequestModel} in case of a validation error.
     *
     * @param signUpRequestModel form contents as {@link SignUpRequestModel}
     * @param bindingResult validation results
     * @param request {@link HttpServletRequest} object to gather additional request information (like redirection URI)
     * @return populated {@link ModelAndView} object
     */
    @PostMapping(PATH_SIGNUP)
    public ModelAndView processSignUp(@ModelAttribute @Valid SignUpRequestModel signUpRequestModel, BindingResult bindingResult, HttpServletRequest request) {

        ModelAndView modelAndView;
        if (bindingResult.hasErrors()) {
            signUpRequestModel.setValidationFailed(true);
            modelAndView = renderSignUpForm(signUpRequestModel);
        } else {
            SignUpResult signUpResult = authenticationService.signUp(signUpRequestModel, request);
            String redirectURL = String.format(VIEW_REDIRECT_SIGNUP_TEMPLATE, signUpResult.getRedirectURI(), signUpResult.getSignUpStatus());
            modelAndView = new ModelAndView(redirectURL);
        }

        return modelAndView;
    }

    /**
     * GET /password-reset
     * Renders the password reset request form.
     *
     * @return populated {@link ModelAndView} object
     */
    @GetMapping(PATH_PASSWORD_RESET)
    public ModelAndView renderPasswordResetRequestForm(@ModelAttribute PasswordResetRequestModel passwordResetRequestModel) {


        return new ModelAndView(VIEW_PW_RESET_REQUEST, Map.of(
                ATTRIBUTE_RECAPTCHA_SITE_KEY, recaptchaSiteKey,
                ATTRIBUTE_VALIDATION_ERROR, passwordResetRequestModel.isValidationFailed()
        ));
    }

    /**
     * POST /password-reset
     * Processes the password reset request. Renders the password reset request form with
     * current {@link PasswordResetRequestModel} model in case of validation error.
     *
     * @return populated {@link ModelAndView} object
     */
    @PostMapping(PATH_PASSWORD_RESET)
    public ModelAndView processPasswordResetRequest(@ModelAttribute @Valid PasswordResetRequestModel passwordResetRequestModel,
                                                    BindingResult bindingResult, HttpServletRequest request) {

        ModelAndView modelAndView;
        if (bindingResult.hasErrors()) {
            passwordResetRequestModel.setValidationFailed(true);
            modelAndView = renderPasswordResetRequestForm(passwordResetRequestModel);
        } else {
            authenticationService.requestPasswordReset(passwordResetRequestModel, request);
            modelAndView = new ModelAndView(VIEW_PW_RESET_ACK);
        }

        return modelAndView;
    }

    /**
     * GET /password-reset/confirmation?token
     * Renders the password reset confirmation (password change) form.
     *
     * This endpoint is already and requires a "reclaim" token as the token parameter.
     *
     * @return populated {@link ModelAndView} object
     */
    @GetMapping(value = PATH_PASSWORD_RESET_CONFIRMATION, params = QUERY_PARAMETER_TOKEN)
    public ModelAndView renderPasswordResetConfirmationForm(@ModelAttribute PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel) {

        return new ModelAndView(VIEW_PW_RESET_CONFIRM, Map.of(
                ATTRIBUTE_RECAPTCHA_SITE_KEY, recaptchaSiteKey,
                ATTRIBUTE_VALIDATION_ERROR, passwordResetConfirmationRequestModel.isValidationFailed()
        ));
    }

    /**
     * POST /password-reset/confirmation?token
     * Processes the password reset confirmation (password change) request. Renders the password reset confirmation form
     * in case of validation error.
     *
     * This endpoint is protected and requires a "reclaim" token as the token parameter.
     *
     * @return populated {@link ModelAndView} object (redirection to the login page)
     */
    @PostMapping(value = PATH_PASSWORD_RESET_CONFIRMATION, params = QUERY_PARAMETER_TOKEN)
    public ModelAndView processPasswordResetConfirmation(@ModelAttribute @Valid PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel,
                                                         BindingResult bindingResult, HttpServletRequest request) {

        ModelAndView modelAndView;
        if (bindingResult.hasErrors()) {
            passwordResetConfirmationRequestModel.setValidationFailed(true);
            modelAndView = renderPasswordResetConfirmationForm(passwordResetConfirmationRequestModel);
        } else {
            authenticationService.confirmPasswordReset(passwordResetConfirmationRequestModel, request);
            modelAndView = new ModelAndView(VIEW_REDIRECT_LOGIN);
        }

        return modelAndView;
    }
}
