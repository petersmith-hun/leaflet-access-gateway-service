package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetConfirmationRequestModel;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetSuccess;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.token.TokenTracker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link AccountRequestHandler} implementation processing password reset confirmation requests.
 *
 * The implementation does the following steps:
 *  - Identifies the relevant user account based on the token stored in the security context.
 *  - Updates and stores the new password (if account exists).
 *  - Sends a notification to the user about the successful reset.
 *  - Cleans up the security context and revokes the reclaim token.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class PasswordResetConfirmationAccountRequestHandler implements AccountRequestHandler<PasswordResetConfirmationRequestModel, Void> {

    private final UserDAO userDAO;
    private final NotificationAdapter notificationAdapter;
    private final PasswordEncoder passwordEncoder;
    private final TokenTracker tokenTracker;

    @Autowired
    public PasswordResetConfirmationAccountRequestHandler(UserDAO userDAO, NotificationAdapter notificationAdapter,
                                                          PasswordEncoder passwordEncoder, TokenTracker tokenTracker) {
        this.userDAO = userDAO;
        this.notificationAdapter = notificationAdapter;
        this.passwordEncoder = passwordEncoder;
        this.tokenTracker = tokenTracker;
    }

    @Override
    public Void processAccountRequest(PasswordResetConfirmationRequestModel passwordResetConfirmationRequestModel) {

        TokenClaims claims = extractTokenClaims();
        Optional<User> userOptional = userDAO.findByEmail(claims.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(passwordResetConfirmationRequestModel.getPassword()));
            userDAO.save(user);
            sendPasswordResetConfirmationNotification(user);
            log.info("Password successfully reset for user by ID={}", user.getId());
        } else {
            log.warn("User account identified by email [{}] does not exist", claims.getEmail());
        }

        tokenTracker.revokeToken(claims.getTokenID());
        SecurityContextHolder.clearContext();

        return null;
    }

    private TokenClaims extractTokenClaims() {
        return (TokenClaims) SecurityContextHolder.getContext().getAuthentication().getDetails();
    }

    private void sendPasswordResetConfirmationNotification(User user) {

        notificationAdapter.successfulPasswordReset(PasswordResetSuccess.builder()
                .username(user.getUsername())
                .participant(user.getEmail())
                .build());
    }
}
