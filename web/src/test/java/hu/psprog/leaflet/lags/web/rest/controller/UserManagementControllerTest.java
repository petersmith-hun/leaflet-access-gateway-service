package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.request.UpdateRoleRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link UserManagementController}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    private static final long USER_ID = 1L;
    private static final UserRequest USER_REQUEST = UserRequest.builder().build();

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private UserManagementController userManagementController;

    @Test
    public void shouldGetUsers() {

        // when
        userManagementController.getUsers(1);

        // then
        userManagementService.getUsers(1);
    }

    @Test
    public void shouldGetUser() {

        // when
        userManagementController.getUser(USER_ID);

        // then
        userManagementService.getUser(USER_ID);
    }

    @Test
    public void shouldCreateUser() {

        // when
        userManagementController.createUser(USER_REQUEST);

        // then
        userManagementService.createUser(USER_REQUEST);
    }

    @Test
    public void shouldChangeUserRole() {

        // given
        var updateRoleRequestModel = new UpdateRoleRequestModel();
        updateRoleRequestModel.setRole(LegacyRole.ADMIN);

        // when
        userManagementController.changeUserRole(USER_ID, updateRoleRequestModel);

        // then
        userManagementService.updateUserRole(USER_ID, LegacyRole.ADMIN);
    }

    @Test
    public void shouldEnableUser() {

        // when
        userManagementController.enableUser(USER_ID);

        // then
        userManagementService.updateUserStatus(USER_ID, true);
    }

    @Test
    public void shouldDisableUser() {

        // when
        userManagementController.disableUser(USER_ID);

        // then
        userManagementService.updateUserStatus(USER_ID, false);
    }
}
