package hu.psprog.leaflet.lags.web.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import static hu.psprog.leaflet.lags.web.rest.controller.BaseController.PATH_LOGIN;

/**
 * Controller for authentication related operations.
 *
 * @author Peter Smith
 */
@Controller
public class AuthenticationController {

    private static final String VIEW_LOGIN = "login";

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
}
