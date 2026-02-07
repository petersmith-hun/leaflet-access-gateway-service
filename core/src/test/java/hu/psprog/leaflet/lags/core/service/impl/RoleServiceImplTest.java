package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.RoleMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.persistence.dao.RoleDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link RoleServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    private static final UUID ROLE_ID = UUID.randomUUID();
    private static final UUID PERMISSION_ID = UUID.randomUUID();

    @Mock
    private RoleDAO roleDAO;

    @Mock
    private PermissionDAO permissionDAO;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    public void shouldGetRoles() {

        // given
        var role = Role.builder().name("role").build();
        var roleResponse = RoleResponse.builder().name("role").build();

        var expectedPage = PageRequest.of(0, 10, Sort.by("name").ascending());
        var expectedResponse = new PageImpl<>(List.of(roleResponse));

        given(roleDAO.findAll(expectedPage)).willReturn(new PageImpl<>(List.of(role)));
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.getRoles(1);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldGetRolesUnpaged() {

        // given
        var role = Role.builder().name("role").build();
        var roleResponse = RoleResponse.builder().name("role").build();

        var expectedPage = Pageable.unpaged(Sort.by("name").ascending());
        var expectedResponse = new PageImpl<>(List.of(roleResponse));

        given(roleDAO.findAll(expectedPage)).willReturn(new PageImpl<>(List.of(role)));
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.getRoles(0);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldGetRole() {

        // given
        var role = Role.builder().name("role").build();
        var expectedRoleResponse = RoleResponse.builder().name("role").build();

        given(roleMapper.map(role)).willReturn(expectedRoleResponse);
        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));

        // when
        var result = roleService.getRole(ROLE_ID);

        // then
        assertThat(result, equalTo(expectedRoleResponse));
    }

    @Test
    public void shouldGetRoleThrowExceptionIfMissing() {

        // given
        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.empty());

        // when
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRole(ROLE_ID));

        // then
        // exception expected
    }

    @Test
    public void shouldCreateRole() {

        // given
        var roleRequest = RoleRequest.builder().name("role").build();
        var role = Role.builder().name("role").build();
        var savedRole = Role.builder().id(UUID.randomUUID()).name("role").build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleMapper.map(roleRequest)).willReturn(role);
        given(roleDAO.save(role)).willReturn(savedRole);
        given(roleMapper.map(savedRole)).willReturn(roleResponse);

        // when
        var result = roleService.createRole(roleRequest);

        // then
        assertThat(result, equalTo(roleResponse));
    }

    @Test
    public void shouldCreateRoleThrowExceptionOnDuplicateByName() {

        // given
        var roleRequest = RoleRequest.builder().name("role").build();
        var role = Role.builder().name("role").build();

        given(roleMapper.map(roleRequest)).willReturn(role);
        given(roleDAO.save(role)).willThrow(DataIntegrityViolationException.class);

        // when
        assertThrows(ConflictingResourceException.class, () -> roleService.createRole(roleRequest));

        // then
        // exception expected
    }

    @Test
    public void shouldEditRole() {

        // given
        var roleRequest = RoleRequest.builder()
                .name("role-modified")
                .description("description-modified")
                .build();
        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role")
                .build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.editRole(ROLE_ID, roleRequest);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.getName(), equalTo("role-modified"));
        assertThat(role.getDescription(), equalTo("description-modified"));
    }

    @Test
    public void shouldEditRoleThrowExceptionIfMissing() {

        // given
        var roleRequest = RoleRequest.builder()
                .name("role-modified")
                .description("description-modified")
                .build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.empty());

        // when
        assertThrows(ResourceNotFoundException.class, () -> roleService.editRole(ROLE_ID, roleRequest));

        // then
        // exception expected
    }

    @Test
    public void shouldUpdateRoleStatusToEnabled() {

        // given
        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role")
                .enabled(false)
                .build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.updateRoleStatus(ROLE_ID, true);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.isEnabled(), is(true));
    }

    @Test
    public void shouldUpdateRoleStatusToDisabled() {

        // given
        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role")
                .enabled(true)
                .build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.updateRoleStatus(ROLE_ID, false);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.isEnabled(), is(false));
    }

    @Test
    public void shouldMarkAsLocalDefault() {

        // given
        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role")
                .localDefault(false)
                .externalDefault(false)
                .build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.markAsLocalDefault(ROLE_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.isLocalDefault(), is(true));
        assertThat(role.isExternalDefault(), is(false));

        verify(roleDAO).removeCurrentLocalDefaultFlag();
    }

    @Test
    public void shouldMarkAsExternalDefault() {

        // given
        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role")
                .localDefault(false)
                .externalDefault(false)
                .build();
        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.markAsExternalDefault(ROLE_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.isLocalDefault(), is(false));
        assertThat(role.isExternalDefault(), is(true));

        verify(roleDAO).removeCurrentExternalDefaultFlag();
    }

    @Test
    public void shouldAssignPermission() {

        // given
        var permissionToAssign = Permission.builder()
                .id(PERMISSION_ID)
                .name("permission1")
                .build();

        var role = Role.builder()
                .name("role1")
                .permissions(new LinkedList<>(List.of(
                        Permission.builder().id(UUID.randomUUID()).build()
                )))
                .build();

        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(permissionDAO.findByID(PERMISSION_ID)).willReturn(Optional.of(permissionToAssign));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.assignPermission(ROLE_ID, PERMISSION_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.getPermissions().size(), equalTo(2));
        assertThat(role.getPermissions().getLast(), equalTo(permissionToAssign));
    }

    @Test
    public void shouldAssignPermissionSkipChangeIfAlreadyAssigned() {

        // given
        var permissionToAssign = Permission.builder()
                .id(PERMISSION_ID)
                .name("permission1")
                .build();

        var role = Role.builder()
                .name("role1")
                .permissions(new LinkedList<>(List.of(
                        permissionToAssign,
                        Permission.builder().id(UUID.randomUUID()).build()
                )))
                .build();

        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.assignPermission(ROLE_ID, PERMISSION_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.getPermissions().size(), equalTo(2));
        assertThat(role.getPermissions().getFirst(), equalTo(permissionToAssign));

        verifyNoMoreInteractions(roleDAO);
        verifyNoInteractions(permissionDAO);
    }

    @Test
    public void shouldUnassignPermission() {

        // given
        var permissionToUnassign = Permission.builder()
                .id(PERMISSION_ID)
                .name("permission1")
                .build();

        var role = Role.builder()
                .name("role1")
                .permissions(new LinkedList<>(List.of(
                        Permission.builder().id(UUID.randomUUID()).build(),
                        permissionToUnassign
                )))
                .build();

        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(permissionDAO.findByID(PERMISSION_ID)).willReturn(Optional.of(permissionToUnassign));
        given(roleDAO.save(role)).willReturn(role);
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.unassignPermission(ROLE_ID, PERMISSION_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.getPermissions().size(), equalTo(1));
        assertThat(role.getPermissions().getFirst(), not(equalTo(permissionToUnassign)));
    }

    @Test
    public void shouldUnassignPermissionSkipChangeIfAlreadyUnassigned() {

        // given
        var otherPermissionID = UUID.randomUUID();
        var role = Role.builder()
                .name("role1")
                .permissions(new LinkedList<>(List.of(
                        Permission.builder().id(otherPermissionID).build()
                )))
                .build();

        var roleResponse = RoleResponse.builder().name("role").build();

        given(roleDAO.findByID(ROLE_ID)).willReturn(Optional.of(role));
        given(roleMapper.map(role)).willReturn(roleResponse);

        // when
        var result = roleService.unassignPermission(ROLE_ID, PERMISSION_ID);

        // then
        assertThat(result, equalTo(roleResponse));
        assertThat(role.getPermissions().size(), equalTo(1));
        assertThat(role.getPermissions().getFirst().getId(), equalTo(otherPermissionID));

        verifyNoMoreInteractions(roleDAO);
        verifyNoInteractions(permissionDAO);
    }

    @Test
    public void shouldDeleteRole() {

        // when
        roleService.deleteRole(ROLE_ID);

        // then
        verify(roleDAO).delete(ROLE_ID);
    }
}
