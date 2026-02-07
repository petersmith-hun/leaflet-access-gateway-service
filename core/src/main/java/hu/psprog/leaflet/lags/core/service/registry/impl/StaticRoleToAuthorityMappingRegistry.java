package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            "read:comments:own",
            "read:users:own",
            "write:comments:own",
            "write:users:own"
    };

    private static final String[] EXTERNAL_USER_AUTHORITIES = {
            "read:comments:own",
            "read:users:own",
            "write:comments:own"
    };

    private static final String[] EDITOR_AUTHORITIES = Stream.concat(Stream.of(USER_AUTHORITIES), Stream.of(
            "read:categories",
            "read:comments",
            "read:documents",
            "read:entries",
            "read:tags",
            "write:categories",
            "write:comments",
            "write:documents",
            "write:entries",
            "write:tags"
    )).toList().toArray(String[]::new);

    private static final String[] ADMIN_AUTHORITIES = Stream.concat(Stream.of(EDITOR_AUTHORITIES), Stream.of(
            "read:admin",
            "read:users",
            "write:admin",
            "write:users"
    )).toList().toArray(String[]::new);

    private static final Map<LegacyRole, List<GrantedAuthority>> ROLE_TO_AUTHORITY_LIST_MAP = Map.of(
            LegacyRole.USER, AuthorityUtils.createAuthorityList(USER_AUTHORITIES),
            LegacyRole.EXTERNAL_USER, AuthorityUtils.createAuthorityList(EXTERNAL_USER_AUTHORITIES),
            LegacyRole.EDITOR, AuthorityUtils.createAuthorityList(EDITOR_AUTHORITIES),
            LegacyRole.ADMIN, AuthorityUtils.createAuthorityList(ADMIN_AUTHORITIES)
    );

    @Override
    public List<GrantedAuthority> getAuthoritiesForRole(LegacyRole role) {
        return ROLE_TO_AUTHORITY_LIST_MAP.getOrDefault(role, Collections.emptyList());
    }
}
