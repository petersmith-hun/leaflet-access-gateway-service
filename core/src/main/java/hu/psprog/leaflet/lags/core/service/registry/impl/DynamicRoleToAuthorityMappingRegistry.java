package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * "Dynamic" implementation of {@link RoleToAuthorityMappingRegistry}.
 * Returns the permissions assigned to the current user's role.
 *
 * @author Peter Smith
 */
@Component
public class DynamicRoleToAuthorityMappingRegistry implements RoleToAuthorityMappingRegistry {

    @Override
    public List<GrantedAuthority> getAuthoritiesForRole(Role role) {

        return role.getPermissions()
                .stream()
                .map(Permission::getName)
                .sorted(Comparator.naturalOrder())
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
