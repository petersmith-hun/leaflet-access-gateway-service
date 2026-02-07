package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RoleManagementController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RoleManagementControllerTest {

    private static final UUID ROLE_ID = UUID.randomUUID();
    private static final UUID PERMISSION_ID = UUID.randomUUID();
    private static final RoleRequest ROLE_REQUEST = RoleRequest.builder().build();

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleManagementController roleManagementController;

    @Test
    public void shouldGetRoles() {

        // when
        roleManagementController.getRoles(1);

        // then
        verify(roleService).getRoles(1);
    }

    @Test
    public void shouldGetRole() {

        // when
        roleManagementController.getRole(ROLE_ID);

        // then
        verify(roleService).getRole(ROLE_ID);
    }

    @Test
    public void shouldCreateRole() {

        // when
        roleManagementController.createRole(ROLE_REQUEST);

        // then
        verify(roleService).createRole(ROLE_REQUEST);
    }

    @Test
    public void shouldEditRole() {

        // when
        roleManagementController.editRole(ROLE_ID, ROLE_REQUEST);

        // then
        verify(roleService).editRole(ROLE_ID, ROLE_REQUEST);
    }

    @Test
    public void shouldEnableRole() {

        // when
        roleManagementController.enableRole(ROLE_ID);

        // then
        verify(roleService).updateRoleStatus(ROLE_ID, true);
    }

    @Test
    public void shouldDisableRole() {

        // when
        roleManagementController.disableRole(ROLE_ID);

        // then
        verify(roleService).updateRoleStatus(ROLE_ID, false);
    }

    @Test
    public void shouldMarkRoleAsLocalDefault() {

        // when
        roleManagementController.markRoleAsLocalDefault(ROLE_ID);

        // then
        verify(roleService).markAsLocalDefault(ROLE_ID);
    }

    @Test
    public void shouldMarkRoleAsExternalDefault() {

        // when
        roleManagementController.markRoleAsExternalDefault(ROLE_ID);

        // then
        verify(roleService).markAsExternalDefault(ROLE_ID);
    }

    @Test
    public void shouldAssignPermission() {

        // when
        roleManagementController.assignPermission(ROLE_ID, PERMISSION_ID);

        // then
        verify(roleService).assignPermission(ROLE_ID, PERMISSION_ID);
    }

    @Test
    public void shouldUnassignPermission() {

        // when
        roleManagementController.unassignPermission(ROLE_ID, PERMISSION_ID);

        // then
        verify(roleService).unassignPermission(ROLE_ID, PERMISSION_ID);
    }

    @Test
    public void shouldDeleteRole() {

        // when
        roleManagementController.deleteRole(ROLE_ID);

        // then
        verify(roleService).deleteRole(ROLE_ID);
    }
}
