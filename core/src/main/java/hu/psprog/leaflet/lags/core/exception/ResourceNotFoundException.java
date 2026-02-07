package hu.psprog.leaflet.lags.core.exception;

import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;

import java.util.UUID;

/**
 * Exception to be thrown when the requested resource is not found.
 *
 * @author Peter Smith
 */
public class ResourceNotFoundException extends RuntimeException {

    private ResourceNotFoundException(ManagedResourceType resourceType, UUID id) {
        super("%s by ID=%s not found: ".formatted(resourceType.getDisplayName(), id));
    }

    /**
     * Creates a {@link ResourceNotFoundException} for a missing role.
     *
     * @param id requested resource ID
     * @return populated {@link ResourceNotFoundException} instance
     */
    public static ResourceNotFoundException role(UUID id) {
        return new ResourceNotFoundException(ManagedResourceType.ROLE, id);
    }

    /**
     * Creates a {@link ResourceNotFoundException} for a missing permission.
     *
     * @param id requested resource ID
     * @return populated {@link ResourceNotFoundException} instance
     */
    public static ResourceNotFoundException permission(UUID id) {
        return new ResourceNotFoundException(ManagedResourceType.PERMISSION, id);
    }

    /**
     * Creates a {@link ResourceNotFoundException} for a missing application.
     *
     * @param id requested resource ID
     * @return populated {@link ResourceNotFoundException} instance
     */
    public static ResourceNotFoundException application(UUID id) {
        return new ResourceNotFoundException(ManagedResourceType.APPLICATION, id);
    }
}
