package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.web.model.ProfileScreen;
import hu.psprog.leaflet.lags.web.utility.ReturnDirectiveUtility;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Factory implementation to create {@link ModelAndView} objects for certain profile pages.
 *
 * @author Peter Smith
 */
@Component
public class ProfileViewFactory {

    private static final String LOGOUT_REFERENCE_URL_TEMPLATE = "/profile?return=%s";

    private final ReturnDirectiveUtility returnDirectiveUtility;

    @Autowired
    public ProfileViewFactory(ReturnDirectiveUtility returnDirectiveUtility) {
        this.returnDirectiveUtility = returnDirectiveUtility;
    }

    /**
     * Creates a logout reference using the current return directive. Can be used to keep the return directive even if
     * the user signs out then back in.
     *
     * @param request {@link HttpServletRequest} object to extract the session, expected to contain the return directive
     * @return generated and base64 encoded logout reference to be appended to the logout URL
     */
    public String createLogoutReference(HttpServletRequest request) {

        byte[] redirectURL = String
                .format(LOGOUT_REFERENCE_URL_TEMPLATE, returnDirectiveUtility.getRequiredReturnDirective(request))
                .getBytes(StandardCharsets.UTF_8);

        return Base64.getEncoder()
                .encodeToString(redirectURL);
    }

    /**
     * Creates a redirection view for the given profile screen and operation result.
     *
     * @param profileScreen {@link ProfileScreen} definition to extract redirection information
     * @param result operation result as {@link ProfileOperationResult}
     * @return populated {@link ModelAndView} for redirection
     */
    public ModelAndView createRedirection(ProfileScreen profileScreen, ProfileOperationResult result) {

        return result == ProfileOperationResult.SUCCESS
                ? redirectToMainScreen(profileScreen)
                : redirectTo(profileScreen, result);
    }

    /**
     * Creates a standard view based on the given {@link ProfileScreen} definition
     *
     * @param profileScreen {@link ProfileScreen} definition to extract view path
     * @return populated {@link ModelAndView} for normal view rendering
     */
    public ModelAndView createView(ProfileScreen profileScreen) {
        return new ModelAndView(profileScreen.getViewPath());
    }

    /**
     * Creates a standard view based on the given {@link ProfileScreen} definition
     *
     * @param profileScreen {@link ProfileScreen} definition to extract view path
     * @param parameters view hydration parameters
     * @return populated {@link ModelAndView} for normal view rendering
     */
    public ModelAndView createView(ProfileScreen profileScreen, Map<String, Object> parameters) {
        return new ModelAndView(profileScreen.getViewPath(), parameters);
    }

    /**
     * Creates a redirection view to the main screen from the given one. Always redirects to the main profile screen,
     * including the executed profile operation's operation code (extracted from the {@link ProfileScreen} parameter).
     * Can be used to render a success notification.
     *
     * @param fromScreen {@link ProfileScreen} definition to extract the operation code of the successful profile operation
     * @return populated {@link ModelAndView} for redirection
     */
    public ModelAndView redirectToMainScreen(ProfileScreen fromScreen) {
        return new ModelAndView(ProfileScreen.MAIN.getRedirectUrl().formatted(fromScreen.getOperationCode()));
    }

    /**
     * Creates a redirection view to the given screen (usually on failure). Redirects to the screen defined by the given
     * {@link ProfileScreen} definition, including the executed profile operation's result code. Can be used to render
     * a failure notification.
     *
     * @param toScreen {@link ProfileScreen} definition to extract the redirection path of the (failed) profile operation
     * @param profileOperationResult operation result code as {@link ProfileOperationResult}
     * @return populated {@link ModelAndView} for redirection
     */
    public ModelAndView redirectTo(ProfileScreen toScreen, ProfileOperationResult profileOperationResult) {
        return new ModelAndView(toScreen.getRedirectUrl().formatted(profileOperationResult.getResultCode()));
    }
}
