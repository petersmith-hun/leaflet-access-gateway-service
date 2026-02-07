package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper implementation for role objects.
 *
 * @author Peter Smith
 */
@Component
public class RoleMapper extends AbstractCommonMapper {

    private final PermissionMapper permissionMapper;

    @Autowired
    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    /**
     * Maps the given {@link Role} entity to {@link RoleResponse}.
     *
     * @param role source {@link Role} entity
     * @return mapped {@link RoleResponse} object
     */
    public RoleResponse map(Role role) {

        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .localDefault(role.isLocalDefault())
                .externalDefault(role.isExternalDefault())
                .enabled(role.isEnabled())
                .created(convertDate(role.getCreatedAt()))
                .lastModified(convertDate(role.getUpdatedAt()))
                .permissions(role.getPermissions()
                        .stream()
                        .map(permissionMapper::map)
                        .toList())
                .build();
    }

    /**
     * Maps the given {@link RoleRequest} to {@link Role} entity.
     *
     * @param roleRequest source {@link Role} request model
     * @return mapped {@link Role} object
     */
    public Role map(RoleRequest roleRequest) {

        return Role.builder()
                .name(roleRequest.name())
                .description(roleRequest.description())
                .build();
    }
}
