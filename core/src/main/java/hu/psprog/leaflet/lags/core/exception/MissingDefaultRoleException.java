package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception to be thrown when a certain default role is not configured upon user registration.
 *
 * @author Peter Smith
 */
public class MissingDefaultRoleException extends RuntimeException {

    private MissingDefaultRoleException(String message) {
        super(message);
    }

    /**
     * Creates an exception with the message stating the lack of configured internal default user role.
     *
     * @return populated exception
     */
    public static MissingDefaultRoleException internal() {
        return new MissingDefaultRoleException("Default internal user role is not configured");
    }

    /**
     * Creates an exception with the message stating the lack of configured external default user role.
     *
     * @return populated exception
     */
    public static MissingDefaultRoleException external() {
        return new MissingDefaultRoleException("Default external user role is not configured");
    }
}
