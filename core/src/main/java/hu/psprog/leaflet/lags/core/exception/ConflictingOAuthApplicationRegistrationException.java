package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception to be thrown when a write operation on an application registration causes a data integrity error.
 *
 * @author Peter Smith
 */
public class ConflictingOAuthApplicationRegistrationException extends RuntimeException {

    public ConflictingOAuthApplicationRegistrationException(String message) {
        super(message);
    }

    /**
     * The name or the client ID in the definition to be created is already in use.
     *
     * @return exception with the relevant message
     */
    public static ConflictingOAuthApplicationRegistrationException onCreate() {
        return new ConflictingOAuthApplicationRegistrationException("Application definition is conflicting with an already registered application");
    }

    /**
     * The definition to be deleted is referenced by another definition.
     *
     * @return exception with the relevant message
     */
    public static ConflictingOAuthApplicationRegistrationException onDelete() {
        return new ConflictingOAuthApplicationRegistrationException("Application is being referenced by another application, preventing deletion");
    }
}
