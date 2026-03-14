package hu.psprog.leaflet.lags.web.utility;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.web.exception.MissingReturnDirectiveException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Utility implementation handling the "return directive", determining where to redirect the browser after finishing
 * profile management operations.
 *
 * @author Peter Smith
 */
@Component
public class ReturnDirectiveUtility {

    private static final String SESSION_PARAMETER_RETURN_DIRECTIVE = "returnDirective";
    private static final String RETURN_DIRECTIVE_QUERY_PARAMETER = "return";

    private final AuthenticationConfig authenticationConfig;

    @Autowired
    public ReturnDirectiveUtility(AuthenticationConfig authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    /**
     * Checks if the return directive is already present in the session. If not, tries extracting it from the query
     * parameters and checks the presence again. If the return directive is still missing (i.e. there was none present
     * in the query parameters), throws exception.
     *
     * @param request {@link HttpServletRequest} object to extract the session and the query parameters, in order to
     *                                          find the return directive
     * @throws MissingReturnDirectiveException if return directive could not be found in either places
     */
    public void ensureReturnDirective(HttpServletRequest request) {

        String returnDirectiveFromQuery = request.getParameter(RETURN_DIRECTIVE_QUERY_PARAMETER);

        if (getReturnDirective(request).isPresent() && Objects.isNull(returnDirectiveFromQuery)) {
            return;
        }

        request.getSession().setAttribute(SESSION_PARAMETER_RETURN_DIRECTIVE, returnDirectiveFromQuery);
        getRequiredReturnDirective(request);
    }

    /**
     * Retrieves the current return directive from the session. If it's not present, throws exception
     *
     * @param request {@link HttpServletRequest} object to extract the session, expected to contain the return directive
     * @return resolved return directive from the session
     * @throws MissingReturnDirectiveException if return directive could not be found in the session
     */
    public String getRequiredReturnDirective(HttpServletRequest request) {

        return getReturnDirective(request)
                .orElseThrow(MissingReturnDirectiveException::new);
    }

    /**
     * Returns the corresponding return definition of the currently active return directive.
     *
     * @param request {@link HttpServletRequest} object to extract the session, expected to contain the return directive
     * @return resolved return definition
     * @throws MissingReturnDirectiveException if return directive could not be found in the session, or there's no
     * configured return definition for the active return directive
     */
    public AuthenticationConfig.ReturnDefinition getReturnDefinition(HttpServletRequest request) {

        return getReturnDirective(request)
                .map(authenticationConfig.getReturnDefinitions()::get)
                .orElseThrow(MissingReturnDirectiveException::new);
    }

    private Optional<String> getReturnDirective(HttpServletRequest request) {

        return Optional.ofNullable(request.getSession())
                .map(session -> session.getAttribute(SESSION_PARAMETER_RETURN_DIRECTIVE))
                .map(Object::toString);
    }
}
