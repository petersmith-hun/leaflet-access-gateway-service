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
                "read:categories:public",
                "read:comments:public",
                "read:documents:public",
                "read:entries:public",
                "read:tags:public",
                "read:users:self",
                "write:comments:create",
                "write:comments:edit:self",
                "write:users:self"
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnEditorAuthorities() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.EDITOR);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:categories:public",
                "read:comments:public",
                "read:documents:public",
                "read:entries:public",
                "read:tags:public",
                "read:users:self",
                "write:comments:create",
                "write:comments:edit:self",
                "write:users:self",
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
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnAdminAuthorities() {

        // when
        List<GrantedAuthority> result = staticRoleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.ADMIN);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:categories:public",
                "read:comments:public",
                "read:documents:public",
                "read:entries:public",
                "read:tags:public",
                "read:users:self",
                "write:comments:create",
                "write:comments:edit:self",
                "write:users:self",
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
                "write:tags:edit:all",
                "read:admin:all",
                "write:admin:all"
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnEmptyAuthorityListForNonConfiguredRole() {

    }
}
