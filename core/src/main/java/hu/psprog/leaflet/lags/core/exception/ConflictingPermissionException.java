package hu.psprog.leaflet.lags.core.exception;

/**
 * Exception to be thrown when a write operation on a permission causes a data integrity error.
 *
 * @author Peter Smith
 */
public class ConflictingPermissionException extends RuntimeException {

    public ConflictingPermissionException(String message) {
        super(message);
    }

    /**
     * The name of the permission to be created is already in use.
     *
     * @return exception with the relevant message
     */
    public static ConflictingPermissionException onCreate() {
        return new ConflictingPermissionException("Permission name is conflicting with an already registered permission");
    }

    /**
     * The permission to be deleted is referenced by another entity.
     *
     * @return exception with the relevant message
     */
    public static ConflictingPermissionException onDelete() {
        return new ConflictingPermissionException("Permission is being referenced by a registered application or a role, preventing deletion");
    }
}
