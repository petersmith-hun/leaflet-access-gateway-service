package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.PasswordUpdateModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.UserMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDAO userDAO;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void shouldGetUsers() {

        // given
        var user = User.builder().username("user1").build();
        var userResponse = UserDetailsResponse.builder().username("user1").build();

        var expectedPage = PageRequest.of(0, 10, Sort.by("username").ascending());
        var expectedResponse = new PageImpl<>(List.of(userResponse));

        given(userDAO.findAll(expectedPage)).willReturn(new PageImpl<>(List.of(user)));
        given(userMapper.map(user)).willReturn(userResponse);

        // when
        var result = userService.getUsers(1);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldGetUsersUnpaged() {

        // given
        var user = User.builder().username("user1").build();
        var userResponse = UserDetailsResponse.builder().username("user1").build();

        var expectedPage = Pageable.unpaged(Sort.by("username").ascending());
        var expectedResponse = new PageImpl<>(List.of(userResponse));

        given(userDAO.findAll(expectedPage)).willReturn(new PageImpl<>(List.of(user)));
        given(userMapper.map(user)).willReturn(userResponse);

        // when
        var result = userService.getUsers(0);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldGetUser() {

        // given
        var user = User.builder().username("user1").build();
        var expectedUserResponse = UserDetailsResponse.builder().username("user1").build();

        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.map(user)).willReturn(expectedUserResponse);

        // when
        var result = userService.getUser(USER_ID);

        // then
        assertThat(result, equalTo(expectedUserResponse));
    }

    @Test
    public void shouldGetUserThrowExceptionIfMissing() {

        // given
        given(userDAO.findByID(USER_ID)).willReturn(Optional.empty());

        // when
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser(USER_ID));

        // then
        // expected exception
    }

    @Test
    public void shouldCreateUser() {

        // given
        var request = UserRequest.builder()
                .username("user-new")
                .email("user123@dev.local")
                .build();
        var user = User.builder().username("user-new").build();
        var savedUser = User.builder().id(123L).username("user-new").build();
        var userResponse = UserDetailsResponse.builder().username("user-new").build();
        var expectedPasswordResetRequest = new PasswordResetRequestModel();
        expectedPasswordResetRequest.setEmail("user123@dev.local");

        given(userMapper.map(request)).willReturn(user);
        given(userDAO.save(user)).willReturn(savedUser);
        given(userMapper.map(savedUser)).willReturn(userResponse);

        // when
        var result = userService.createUser(request);

        // then
        assertThat(result, equalTo(userResponse));

        verify(passwordResetRequestAccountRequestHandler).processAccountRequest(expectedPasswordResetRequest);
    }

    @Test
    public void shouldCreateUserThrowExceptionOnDuplicateByName() {

        // given
        var request = UserRequest.builder().username("user-new").build();
        var user = User.builder().username("user-new").build();

        given(userMapper.map(request)).willReturn(user);
        given(userDAO.save(user)).willThrow(DataIntegrityViolationException.class);

        // when
        assertThrows(ConflictingResourceException.class, () -> userService.createUser(request));

        // then
        // exception expected
        verifyNoInteractions(passwordResetRequestAccountRequestHandler);
    }

    @Test
    public void shouldUpdateUserRole() {

        // given
        var user = User.builder()
                .username("user1")
                .role(LegacyRole.ADMIN)
                .build();
        var expectedUserResponse = UserDetailsResponse.builder().username("user1").build();

        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.map(user)).willReturn(expectedUserResponse);

        // when
        var result = userService.updateUserRole(USER_ID, LegacyRole.EDITOR);

        // then
        assertThat(result, equalTo(expectedUserResponse));
        assertThat(user.getRole(), equalTo(LegacyRole.EDITOR));

        verify(userDAO).save(user);
    }

    @Test
    public void shouldUpdateUserRoleThrowExceptionIfMissing() {

        // given
        given(userDAO.findByID(USER_ID)).willReturn(Optional.empty());

        // when
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRole(USER_ID, LegacyRole.EDITOR));

        // then
        // exception expected
    }

    @Test
    public void shouldUpdateUserStatusToEnabled() {

        // given
        var user = User.builder()
                .username("user1")
                .enabled(false)
                .build();
        var expectedUserResponse = UserDetailsResponse.builder().username("user1").build();

        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.map(user)).willReturn(expectedUserResponse);

        // when
        var result = userService.updateUserStatus(USER_ID, true);

        // then
        assertThat(result, equalTo(expectedUserResponse));
        assertThat(user.isEnabled(), is(true));

        verify(userDAO).save(user);
    }

    @Test
    public void shouldUpdateUserStatusToDisabled() {

        // given
        var user = User.builder()
                .username("user1")
                .enabled(true)
                .build();
        var expectedUserResponse = UserDetailsResponse.builder().username("user1").build();

        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.map(user)).willReturn(expectedUserResponse);

        // when
        var result = userService.updateUserStatus(USER_ID, false);

        // then
        assertThat(result, equalTo(expectedUserResponse));
        assertThat(user.isEnabled(), is(false));

        verify(userDAO).save(user);
    }

    @Test
    public void shouldUpdateLastLogin() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();

        given(authentication.getPrincipal()).willReturn(extendedUser);

        // when
        userService.updateLastLogin(authentication);

        // then
        verify(userDAO).updateLastLogin(USER_ID);
    }

    @Test
    public void shouldUpdateLastLoginSkipIfPrincipalIsNotExtendedUser() {

        // given
        given(authentication.getPrincipal()).willReturn(null);

        // when
        userService.updateLastLogin(authentication);

        // then
        verifyNoInteractions(userDAO);
    }

    @Test
    public void shouldUpdateLastLoginSkipIfErrorOccurs() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        doThrow(RuntimeException.class).when(userDAO).updateLastLogin(USER_ID);

        // when
        userService.updateLastLogin(authentication);

        // then
        // silent fallthrough expected
        verifyNoMoreInteractions(userDAO);
    }

    @Test
    public void shouldGetProfile() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder().id(USER_ID).build();
        var profileModel = ProfileModel.builder().username("user1").build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.mapToProfile(user)).willReturn(profileModel);

        // when
        var result = userService.getProfile(authentication);

        // then
        assertThat(result, equalTo(profileModel));
    }

    @Test
    public void shouldGetUserDetails() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder().id(USER_ID).build();
        var userDetails = UserDetailsResponse.builder().username("user1").build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userMapper.map(user)).willReturn(userDetails);

        // when
        var result = userService.getUserDetails(authentication);

        // then
        assertThat(result, equalTo(userDetails));
    }

    @Test
    public void shouldUpdateProfileWithSuccess() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder().id(USER_ID).build();
        var profileModel = ProfileModel.builder()
                .username("user-modified")
                .email("email-modified@dev.local")
                .locale("EN")
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));

        // when
        var result = userService.updateProfile(authentication, profileModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.SUCCESS));
        assertThat(user.getUsername(), equalTo("user-modified"));
        assertThat(user.getEmail(), equalTo("email-modified@dev.local"));
        assertThat(user.getDefaultLocale(), equalTo(SupportedLocale.EN));

        verify(userDAO).save(user);
    }

    @Test
    public void shouldUpdateProfileRespondWithNewEmailInUseStatusOnConflictingEmailAddress() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder().id(USER_ID).build();
        var profileModel = ProfileModel.builder()
                .username("user-modified")
                .email("email-conflicting@dev.local")
                .locale("EN")
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(userDAO.save(user)).willThrow(DataIntegrityViolationException.class);

        // when
        var result = userService.updateProfile(authentication, profileModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.NEW_EMAIL_IN_USE));
    }

    @Test
    public void shouldUpdateProfileWithUnknownErrorOnAnyOtherException() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var profileModel = ProfileModel.builder()
                .username("user-modified")
                .email("email-conflicting@dev.local")
                .locale("EN")
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.empty());

        // when
        var result = userService.updateProfile(authentication, profileModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.UNKNOWN_ERROR));
    }

    @Test
    public void shouldUpdatePasswordWithSuccess() {

        // given
        var currentPassword = "current-pw";
        var currentPasswordEncrypted = "current-pw-encrypted";
        var newPassword = "new-pw";
        var newPasswordEncrypted = "new-pw-encrypted";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .password(currentPasswordEncrypted)
                .build();
        var passwordUpdateModel = PasswordUpdateModel.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .newPasswordConfirmation(newPassword)
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, currentPasswordEncrypted)).willReturn(true);
        given(passwordEncoder.encode(newPassword)).willReturn(newPasswordEncrypted);

        // when
        var result = userService.updatePassword(authentication, passwordUpdateModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.SUCCESS));
        assertThat(user.getPassword(), equalTo(newPasswordEncrypted));

        verify(userDAO).save(user);
    }

    @Test
    public void shouldUpdatePasswordRespondWithCurrentPasswordMismatch() {

        // given
        var wrongCurrentPassword = "wrong-current-pw";
        var currentPasswordEncrypted = "current-pw-encrypted";
        var newPassword = "new-pw";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .password(currentPasswordEncrypted)
                .build();
        var passwordUpdateModel = PasswordUpdateModel.builder()
                .currentPassword(wrongCurrentPassword)
                .newPassword(newPassword)
                .newPasswordConfirmation(newPassword)
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(wrongCurrentPassword, currentPasswordEncrypted)).willReturn(false);

        // when
        var result = userService.updatePassword(authentication, passwordUpdateModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.CURRENT_PASSWORD_MISMATCH));

        verifyNoMoreInteractions(userDAO);
    }

    @Test
    public void shouldUpdatePasswordRespondWithNewPasswordMismatch() {

        // given
        var currentPassword = "current-pw";
        var currentPasswordEncrypted = "current-pw-encrypted";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .password(currentPasswordEncrypted)
                .build();
        var passwordUpdateModel = PasswordUpdateModel.builder()
                .currentPassword(currentPassword)
                .newPassword("new-pw")
                .newPasswordConfirmation("new-pw-wrong-confirmation")
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, currentPasswordEncrypted)).willReturn(true);

        // when
        var result = userService.updatePassword(authentication, passwordUpdateModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.NEW_PASSWORD_MISMATCH));

        verifyNoMoreInteractions(userDAO);
    }

    @Test
    public void shouldUpdatePasswordRespondWithUnknownErrorOnAnyOtherException() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var passwordUpdateModel = PasswordUpdateModel.builder()
                .currentPassword("current-pw")
                .newPassword("new-pw")
                .newPasswordConfirmation("new-pw")
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.empty());

        // when
        var result = userService.updatePassword(authentication, passwordUpdateModel);

        // then
        assertThat(result, equalTo(ProfileOperationResult.UNKNOWN_ERROR));

        verifyNoMoreInteractions(userDAO);
    }

    @Test
    public void shouldDeleteAccountWithSuccessForLocalAccount() {

        // given
        var currentPassword = "current-pw";
        var encryptedPassword = "current-pw-encrypted";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .accountType(AccountType.LOCAL)
                .password(encryptedPassword)
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, encryptedPassword)).willReturn(true);

        // when
        var result =  userService.deleteAccount(authentication, currentPassword);

        // then
        assertThat(result, equalTo(ProfileOperationResult.SUCCESS));

        verify(userDAO).delete(USER_ID);
    }

    @Test
    public void shouldDeleteAccountWithSuccessForExternalAccount() {

        // given
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .accountType(AccountType.GOOGLE)
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));

        // when
        var result =  userService.deleteAccount(authentication, null);

        // then
        assertThat(result, equalTo(ProfileOperationResult.SUCCESS));

        verify(userDAO).delete(USER_ID);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    public void shouldDeleteAccountRespondWithCurrentPasswordMismatch() {

        // given
        var currentPassword = "current-pw";
        var encryptedPassword = "current-pw-encrypted";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();
        var user = User.builder()
                .id(USER_ID)
                .username("user-new-pw")
                .accountType(AccountType.LOCAL)
                .password(encryptedPassword)
                .build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(currentPassword, encryptedPassword)).willReturn(false);

        // when
        var result =  userService.deleteAccount(authentication, currentPassword);

        // then
        assertThat(result, equalTo(ProfileOperationResult.CURRENT_PASSWORD_MISMATCH));

        verifyNoMoreInteractions(userDAO);
    }

    @Test
    public void shouldDeleteAccountRespondWithUnknownErrorOnAnyOtherException() {

        // given
        var currentPassword = "current-pw";
        var extendedUser = ExtendedUser.builder().id(USER_ID).build();

        given(authentication.getPrincipal()).willReturn(extendedUser);
        given(userDAO.findByID(USER_ID)).willReturn(Optional.empty());

        // when
        var result =  userService.deleteAccount(authentication, currentPassword);

        // then
        assertThat(result, equalTo(ProfileOperationResult.UNKNOWN_ERROR));

        verifyNoMoreInteractions(userDAO);
    }
}
