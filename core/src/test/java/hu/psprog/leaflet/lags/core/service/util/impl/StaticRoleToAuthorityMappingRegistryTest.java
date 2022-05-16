package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link StaticRoleToAuthorityMappingRegistry}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class StaticRoleToAuthorityMappingRegistryTest {

    @InjectMocks
    private StaticRoleToAuthorityMappingRegistry staticRoleToAuthorityMappingRegistry;

    @Test
    public void shouldGetAuthoritiesForRoleReturnUserAuthorities() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.USER);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:users:own",
                "write:comments:own",
                "write:users:own"
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnEditorAuthorities() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.EDITOR);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:users:own",
                "write:comments:own",
                "write:users:own",
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
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnAdminAuthorities() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.ADMIN);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:users:own",
                "write:comments:own",
                "write:users:own",
                "read:categories",
                "read:comments",
                "read:documents",
                "read:entries",
                "read:tags",
                "write:categories",
                "write:comments",
                "write:documents",
                "write:entries",
                "write:tags",
                "read:admin",
                "read:users",
                "write:admin",
                "write:users"
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnEmptyAuthorityListForNonConfiguredRole() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.NO_LOGIN);

        // then
        assertThat(result.isEmpty(), is(true));
    }
}
