package hu.psprog.leaflet.lags.core.service.util;

import hu.psprog.leaflet.lags.core.domain.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * User role to authority (allowed scope) list mapping registry interface.
 *
 * @author Peter Smith
 */
public interface RoleToAuthorityMappingRegistry {

    /**
     * Returns the list of allowed OAuth2 scopes wrapped as {@link GrantedAuthority} objects for the given role.
     *
     * @param role user role to retrieve authorities for
     * @return list of allowed OAuth2 scopes wrapped as {@link GrantedAuthority} objects
     */
    List<GrantedAuthority> getAuthoritiesForRole(Role role);
}
