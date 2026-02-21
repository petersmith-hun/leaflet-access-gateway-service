package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.ManagedResourceType;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.UserMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.UserManagementService;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link UserManagementService}.
 *
 * @author Peter Smith
 */
@Slf4j
@Service
public class UserServiceImpl implements UserManagementService {

    private static final Sort USERS_PAGE_SORT = Sort.by("username").ascending();

    private final UserDAO userDAO;
    private final UserMapper userMapper;
    private final AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler;

    @Autowired
    public UserServiceImpl(UserDAO userDAO, UserMapper userMapper,
                           AccountRequestHandler<PasswordResetRequestModel, Void> passwordResetRequestAccountRequestHandler) {
        this.userDAO = userDAO;
        this.userMapper = userMapper;
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
    public UserDetailsResponse updateUserRole(Long userID, LegacyRole role) {

        User currentUserData = findRequiredUser(userID, Function.identity());
        currentUserData.setRole(role);

        exceptionAwareCall(() -> userDAO.save(currentUserData));

        log.info("Role of user {} ({}) has been updated to {}", currentUserData.getEmail(), userID, role);

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
}
