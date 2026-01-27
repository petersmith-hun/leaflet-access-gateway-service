package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper implementation for permission object.
 *
 * @author Peter Smith
 */
@Component
public class PermissionMapper extends AbstractCommonMapper {

    /**
     * Maps the given {@link Permission} entity to {@link PermissionResponse}.
     *
     * @param permission source {@link Permission} entity
     * @return mapped {@link PermissionResponse} object
     */
    public PermissionResponse map(Permission permission) {

        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .enabled(permission.isEnabled())
                .created(convertDate(permission.getCreatedAt()))
                .lastModified(convertDate(permission.getUpdatedAt()))
                .build();
    }

    /**
     * Maps the given {@link PermissionRequest} entity to {@link Permission} to be saved directly in the database.
     *
     * @param permissionRequest source {@link PermissionRequest} entity
     * @return mapped {@link Permission} object
     */
    public Permission map(PermissionRequest permissionRequest) {

        return Permission.builder()
                .name(permissionRequest.name())
                .description(permissionRequest.description())
                .build();
    }
}
