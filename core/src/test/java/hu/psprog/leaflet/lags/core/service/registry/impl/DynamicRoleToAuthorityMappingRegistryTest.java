package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link DynamicRoleToAuthorityMappingRegistry}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class DynamicRoleToAuthorityMappingRegistryTest {

    @InjectMocks
    private DynamicRoleToAuthorityMappingRegistry dynamicRoleToAuthorityMappingRegistry;

    @Test
    public void shouldGetAuthoritiesForRoleReturnAuthoritiesScenario1() {

        // given
        var role = Role.builder()
                .permissions(List.of(
                        permission("read:comments:own"),
                        permission("read:users:own"),
                        permission("write:comments:own"),
                        permission("write:users:own")
                ))
                .build();

        // when
        List<GrantedAuthority> result = dynamicRoleToAuthorityMappingRegistry.getAuthoritiesForRole(role);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:comments:own",
                "read:users:own",
                "write:comments:own",
                "write:users:own"
        )));
    }

    @Test
    public void shouldGetAuthoritiesForRoleReturnAuthoritiesScenario2() {

        // given
        var role = Role.builder()
                .permissions(List.of(
                        permission("read:comments:own"),
                        permission("read:users:own"),
                        permission("write:comments:own"),
                        permission("write:users:own"),
                        permission("read:categories"),
                        permission("read:comments"),
                        permission("read:documents"),
                        permission("read:entries"),
                        permission("read:tags"),
                        permission("write:categories"),
                        permission("write:comments"),
                        permission("write:documents"),
                        permission("write:entries"),
                        permission("write:tags")
                ))
                .build();

        // when
        List<GrantedAuthority> result = dynamicRoleToAuthorityMappingRegistry.getAuthoritiesForRole(role);

        // then
        assertThat(result, equalTo(AuthorityUtils.createAuthorityList(
                "read:categories",
                "read:comments",
                "read:comments:own",
                "read:documents",
                "read:entries",
                "read:tags",
                "read:users:own",
                "write:categories",
                "write:comments",
                "write:comments:own",
                "write:documents",
                "write:entries",
                "write:tags",
                "write:users:own"
        )));
    }

    private Permission permission(String name) {

        return Permission.builder()
                .id(UUID.randomUUID())
                .name(name)
                .build();
    }
}
