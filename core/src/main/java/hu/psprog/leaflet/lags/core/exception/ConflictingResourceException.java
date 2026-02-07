package hu.psprog.leaflet.lags.core.exception;

import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;

/**
 * Exception to be thrown when a write operation on a resource causes a data integrity error.
 *
 * @author Peter Smith
 */
public class ConflictingResourceException extends RuntimeException {

    public ConflictingResourceException(String message) {
        super(message);
    }

    /**
     * The name of the resource to be created is already in use.
     *
     * @return exception with the relevant message
     */
    public static ConflictingResourceException onCreate(ManagedResourceType resourceType) {
        return new ConflictingResourceException("%s is conflicting with an already existing one"
                .formatted(resourceType.getDisplayName()));
    }

    /**
     * The resource to be deleted is referenced by another entity.
     *
     * @return exception with the relevant message
     */
    public static ConflictingResourceException onDelete(ManagedResourceType resourceType) {
        return new ConflictingResourceException("%s is being referenced by an another entity, preventing deletion"
                .formatted(resourceType.getDisplayName()));
    }
}
