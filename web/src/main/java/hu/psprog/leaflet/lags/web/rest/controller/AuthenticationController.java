package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
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
import static hu.psprog.leaflet.lags.core.domain.SecurityConstants.PATH_SIGNUP;

/**
 * Controller for authentication related operations.
 *
 * @author Peter Smith
 */
@Controller
public class AuthenticationController {

    private static final String VIEW_LOGIN = "views/login";
    private static final String VIEW_SIGNUP = "views/signup";
    private static final String SIGNUP_REDIRECT_TEMPLATE = "redirect:%s?signup_status=%s";

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
                "recaptchaSiteKey", recaptchaSiteKey,
                "validationError", signUpRequestModel.isValidationFailed()
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
            String redirectURL = String.format(SIGNUP_REDIRECT_TEMPLATE, signUpResult.getRedirectURI(), signUpResult.getSignUpStatus());
            modelAndView = new ModelAndView(redirectURL);
        }

        return modelAndView;
    }
}
