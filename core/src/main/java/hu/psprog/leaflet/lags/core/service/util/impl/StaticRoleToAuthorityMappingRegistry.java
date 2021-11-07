package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.Role;
import hu.psprog.leaflet.lags.core.service.util.RoleToAuthorityMappingRegistry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * "Static" implementation of {@link RoleToAuthorityMappingRegistry}.
 * Uses a hardcoded mapping of roles to allowed scopes. See USER_AUTHORITIES, EDITOR_AUTHORITIES, and ADMIN_AUTHORITIES for details.
 *
 * @author Peter Smith
 */
@Component
public class StaticRoleToAuthorityMappingRegistry implements RoleToAuthorityMappingRegistry {

    private static final String[] USER_AUTHORITIES = {
            "read:categories:public",
            "read:comments:public",
            "read:documents:public",
            "read:entries:public",
            "read:tags:public",
            "read:users:self",
            "write:comments:create",
            "write:comments:edit:self",
            "write:users:self"
    };

    private static final String[] EDITOR_AUTHORITIES = Stream.concat(Stream.of(USER_AUTHORITIES), Stream.of(
            "read:categories:all",
            "read:comments:all",
            "read:documents:all",
            "read:entries:all",
            "read:tags:all",
            "read:users:all",
            "write:categories:edit:all",
            "write:comments:edit:all",
            "write:documents:edit:all",
            "write:entries:edit:all",
            "write:tags:edit:all"
    )).collect(Collectors.toList()).toArray(String[]::new);

    private static final String[] ADMIN_AUTHORITIES = Stream.concat(Stream.of(EDITOR_AUTHORITIES), Stream.of(
            "read:admin:all",
            "write:admin:all"
    )).collect(Collectors.toList()).toArray(String[]::new);

    private static final Map<Role, List<GrantedAuthority>> ROLE_TO_AUTHORITY_LIST_MAP = Map.of(
            Role.USER, AuthorityUtils.createAuthorityList(USER_AUTHORITIES),
            Role.EDITOR, AuthorityUtils.createAuthorityList(EDITOR_AUTHORITIES),
            Role.ADMIN, AuthorityUtils.createAuthorityList(ADMIN_AUTHORITIES)
    );

    @Override
    public List<GrantedAuthority> getAuthoritiesForRole(Role role) {
        return ROLE_TO_AUTHORITY_LIST_MAP.get(role);
    }
}
