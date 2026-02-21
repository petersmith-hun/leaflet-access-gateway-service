package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
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
}
