package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.domain.notification.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.service.notification.NotificationAdapter;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link AccountRequestHandler} implementation processing password reset requests.
 *
 * The implementation does the following steps:
 *  - Identifies the relevant user account based on the request.
 *  - Generates a reclaim token. Expiration depends on the configuration specific for generating tokens for password reset.
 *  - Sends an email notification to the user, including the token as a clickable URL for the confirmation form.
 *
 * @author Peter Smith
 */
@Component
@Slf4j
public class PasswordResetRequestAccountRequestHandler implements AccountRequestHandler<PasswordResetRequestModel, Void> {

    private final UserDAO userDAO;
    private final NotificationAdapter notificationAdapter;
    private final TokenHandler tokenHandler;
    private final AuthenticationConfig.PasswordResetConfig passwordResetConfig;

    @Autowired
    public PasswordResetRequestAccountRequestHandler(UserDAO userDAO, NotificationAdapter notificationAdapter,
                                                     TokenHandler tokenHandler, AuthenticationConfig authenticationConfig) {
        this.userDAO = userDAO;
        this.notificationAdapter = notificationAdapter;
        this.tokenHandler = tokenHandler;
        this.passwordResetConfig = authenticationConfig.getPasswordReset();
    }

    @Override
    public Void processAccountRequest(PasswordResetRequestModel passwordResetRequestModel) {

        Optional<User> userOptional = userDAO.findByEmail(passwordResetRequestModel.getEmail())
                .filter(user -> user.getAccountType() == AccountType.LOCAL);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            OAuthTokenRequest passwordResetTokenRequest = createPasswordResetTokenRequest();
            TokenClaims claims = createPasswordReclaimTokenClaims(user);
            OAuthTokenResponse reclaimToken = tokenHandler.generateToken(passwordResetTokenRequest, claims, passwordResetConfig.getTokenExpiration());
            sendPasswordResetRequestNotification(user, reclaimToken);
            log.info("Password reset request processed for user identified by ID={}", user.getId());
        } else {
            log.warn("User account identified by email [{}] does not exist or non-local", passwordResetRequestModel.getEmail());
        }

        return null;
    }

    private OAuthTokenRequest createPasswordResetTokenRequest() {

        return OAuthTokenRequest.builder()
                .audience(passwordResetConfig.getAudience())
                .build();
    }

    private TokenClaims createPasswordReclaimTokenClaims(User user) {

        log.info("Issuing reclaim-only access token for user identified by ID={}", user.getId());

        return TokenClaims.builder()
                .scope(SecurityConstants.RECLAIM_AUTHORITY.getAuthority())
                .subject(String.format("password-reset|uid=%s", user.getId()))
                .role(SecurityConstants.RECLAIM_ROLE)
                .email(user.getEmail())
                .username(user.getUsername())
                .userID(user.getId())
                .build();
    }

    private void sendPasswordResetRequestNotification(User user, OAuthTokenResponse reclaimToken) {

        notificationAdapter.passwordResetRequested(PasswordResetRequest.builder()
                .username(user.getUsername())
                .recipient(user.getEmail())
                .token(reclaimToken.getAccessToken())
                .resetLink(passwordResetConfig.getReturnUrl())
                .expiration(calculateExpirationInMinutes(reclaimToken))
                .build());
    }

    private int calculateExpirationInMinutes(OAuthTokenResponse reclaimToken) {
        return reclaimToken.getExpiresIn() / 60;
    }
}
