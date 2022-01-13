package hu.psprog.leaflet.lags.web.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for authentication related operations.
 *
 * @author Peter Smith
 */
@Controller
public class AuthenticationController {

    private static final String VIEW_LOGIN = "login";

    @GetMapping("/login")
    public ModelAndView renderLoginForm() {
        return new ModelAndView(VIEW_LOGIN);
    }
}
