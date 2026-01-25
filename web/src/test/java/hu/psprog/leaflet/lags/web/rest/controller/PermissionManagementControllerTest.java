package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PermissionManagementController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionManagementControllerTest {

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionManagementController permissionManagementController;

    @Test
    public void getPermissions() {

        // given
        var page = 1;

        // when
        permissionManagementController.getPermissions(page);

        // then
        verify(permissionService).getPermissions(page);
    }

    @Test
    public void shouldGetPermission() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionManagementController.getPermission(id);

        // then
        verify(permissionService).getPermission(id);
    }

    @Test
    public void shouldCreatePermission() {

        // given
        var permission = PermissionRequest.builder()
                .name("permission")
                .build();

        // when
        permissionManagementController.createPermission(permission);

        // then
        verify(permissionService).createPermission(permission);
    }

    @Test
    public void shouldEditPermission() {

        // given
        var id = UUID.randomUUID();
        var permission = PermissionRequest.builder()
                .name("permission")
                .build();

        // when
        permissionManagementController.editPermission(id, permission);

        // then
        verify(permissionService).editPermission(id, permission);
    }

    @Test
    public void shouldEnablePermission() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionManagementController.enablePermission(id);

        // then
        verify(permissionService).updatePermissionStatus(id, true);
    }

    @Test
    public void shouldDisablePermission() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionManagementController.disablePermission(id);

        // then
        verify(permissionService).updatePermissionStatus(id, false);
    }

    @Test
    public void shouldDeletePermission() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionManagementController.deletePermission(id);

        // then
        verify(permissionService).deletePermission(id);
    }
}
