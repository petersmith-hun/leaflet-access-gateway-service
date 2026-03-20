package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.PasswordUpdateModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.ProfileOperationResult;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.exception.AuthenticationException;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.UserMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.UserManagementService;
import hu.psprog.leaflet.lags.core.service.UserProfileService;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default combined implementation of {@link UserManagementService} and {@link UserProfileService}.
 *
 * @author Peter Smith
 */
@Slf4j
@Service
public class UserServiceImpl implements UserManagementService, UserProfileService {

    private static final Sort USERS_PAGE_SORT = Sort.by("username").ascending();

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler;

    @Autowired
    public UserServiceImpl(UserDAO userDAO, UserMapper userMapper, PasswordEncoder passwordEncoder,
                           AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler) {
        this.userDAO = userDAO;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetRequestAccountRequestHandler = passwordResetRequestAccountRequestHandler;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDetailsResponse> getUsers(int page) {

        return userDAO.findAll(PaginationUtil.createPageRequest(page, USERS_PAGE_SORT))
                .map(userMapper::map);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsResponse getUser(Long userID) {
        return findRequiredUser(userID, userMapper::map);
    }

    @Override
    @Transactional
    public UserDetailsResponse createUser(UserRequest user) {

        User newUser = userMapper.map(user);
        User savedUser = exceptionAwareCall(() -> userDAO.save(newUser));

        log.info("User '{}' created with ID={}", newUser.getUsername(), savedUser.getId());

        passwordResetRequestAccountRequestHandler.processAccountRequest(PasswordResetRequestModel.internal(user));

        return userMapper.map(savedUser);
    }

    @Override
    @Transactional
    public UserDetailsResponse updateUserRole(Long userID, UUID roleID) {

        User currentUserData = findRequiredUser(userID, Function.identity());
        currentUserData.setRole(Role.builder()
                .id(roleID)
                .build());

        exceptionAwareCall(() -> userDAO.save(currentUserData));

        log.info("Role of user {} ({}) has been updated to {}", currentUserData.getEmail(), userID, roleID);

        return userMapper.map(currentUserData);
    }

    @Override
    @Transactional
    public UserDetailsResponse updateUserStatus(Long userID, boolean enabled) {

        User currentUserData = findRequiredUser(userID, Function.identity());
        currentUserData.setEnabled(enabled);

        exceptionAwareCall(() -> userDAO.save(currentUserData));

        log.info("Status of user {} ({}) updated successfully to enabled={}", currentUserData.getEmail(), userID, enabled);

        return userMapper.map(currentUserData);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateLastLogin(Authentication authentication) {

        try {
            if (authentication.getPrincipal() instanceof ExtendedUser extendedUser) {
                userDAO.updateLastLogin(extendedUser.getId());
            }
        } catch (Exception exception) {
            log.error("Failed to update last login of user", exception);
        }
    }

    @Override
    public ProfileModel getProfile(Authentication authentication) {
        return findRequiredUser(extractUserID(authentication), userMapper::mapToProfile);
    }

    @Override
    public UserDetailsResponse getUserDetails(Authentication authentication) {
        return findRequiredUser(extractUserID(authentication), userMapper::map);
    }

    @Override
    public ProfileOperationResult updateProfile(Authentication authentication, ProfileModel profileModel) {

        ProfileOperationResult operationResult;

        try {
            User user = findRequiredUser(authentication);
            user.setUsername(profileModel.getUsername());
            user.setEmail(profileModel.getEmail());
            user.setDefaultLocale(SupportedLocale.valueOf(profileModel.getLocale()));

            userDAO.save(user);

            log.info("Profile of user '{}' ({}) has been updated", user.getUsername(), user.getId());

            operationResult = ProfileOperationResult.SUCCESS;

        } catch (DataIntegrityViolationException exception) {
            log.error("Updated email address is already in use", exception);
            operationResult = ProfileOperationResult.NEW_EMAIL_IN_USE;

        } catch (Exception exception) {
            log.error("Failed to update user profile", exception);
            operationResult = ProfileOperationResult.UNKNOWN_ERROR;
        }

        return operationResult;
    }

    @Override
    public ProfileOperationResult updatePassword(Authentication authentication, PasswordUpdateModel passwordUpdateModel) {

        ProfileOperationResult operationResult;
        try {
            User user = findRequiredUser(authentication);

            if (isCurrentPasswordDifferent(user, passwordUpdateModel.getCurrentPassword())) {
                log.warn("Current password mismatch for user '{}' (ID={}) during password update", user.getUsername(), user.getId());
                operationResult = ProfileOperationResult.CURRENT_PASSWORD_MISMATCH;

            } else if (isPasswordConfirmationDifferent(passwordUpdateModel)) {
                log.warn("New password mismatch for user '{}' (ID={}) during password update", user.getUsername(), user.getId());
                operationResult = ProfileOperationResult.NEW_PASSWORD_MISMATCH;

            } else {
                user.setPassword(passwordEncoder.encode(passwordUpdateModel.getNewPassword()));
                userDAO.save(user);

                log.info("Password of user '{}' ({}) has been updated", user.getUsername(), user.getId());

                operationResult = ProfileOperationResult.SUCCESS;
            }

        } catch (Exception exception) {
            log.error("Failed to update user password", exception);
            operationResult = ProfileOperationResult.UNKNOWN_ERROR;
        }

        return operationResult;
    }

    @Override
    public ProfileOperationResult deleteAccount(Authentication authentication, String currentPassword) {

        ProfileOperationResult operationResult;
        try {
            User user = findRequiredUser(authentication);

            if (user.getAccountType() == AccountType.LOCAL && isCurrentPasswordDifferent(user, currentPassword)) {
                log.warn("Current password mismatch for user '{}' (ID={}) during account deletion", user.getUsername(), user.getId());
                operationResult = ProfileOperationResult.CURRENT_PASSWORD_MISMATCH;

            } else {
                userDAO.delete(user.getId());

                log.warn("User '{}' ({}) has been deleted", user.getUsername(), user.getId());

                operationResult = ProfileOperationResult.SUCCESS;
            }


        } catch (Exception exception) {
            log.error("Failed to update user password", exception);
            operationResult = ProfileOperationResult.UNKNOWN_ERROR;
        }

        return operationResult;
    }

    private <T> T findRequiredUser(Long userID, Function<User, T> mapperFunction) {

        return userDAO.findByID(userID)
                .map(mapperFunction)
                .orElseThrow(() -> {
                    log.error("User by ID={} not found", userID);
                    return ResourceNotFoundException.user(userID);
                });
    }

    private <T> T exceptionAwareCall(Supplier<T> call) {

        try {
            return call.get();

        } catch (DataIntegrityViolationException exception) {
            log.error("Conflicting user: {}", exception.getMessage(), exception);
            throw ConflictingResourceException.onCreate(ManagedResourceType.USER);
        }
    }

    private User findRequiredUser(Authentication authentication) {
        return findRequiredUser(extractUserID(authentication), Function.identity());
    }

    private Long extractUserID(Authentication authentication) {

        if (authentication.getPrincipal() instanceof ExtendedUser extendedUser) {
            return extendedUser.getId();
        } else {
            throw new AuthenticationException("Could not identify user");
        }
    }

    private boolean isCurrentPasswordDifferent(User user, String currentPassword) {
        return !passwordEncoder.matches(currentPassword, user.getPassword());
    }

    private boolean isPasswordConfirmationDifferent(PasswordUpdateModel passwordUpdateModel) {

        return !passwordUpdateModel.getNewPassword()
                .equals(passwordUpdateModel.getNewPasswordConfirmation());
    }
}
