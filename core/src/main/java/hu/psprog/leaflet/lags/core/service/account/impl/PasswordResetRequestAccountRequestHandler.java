package hu.psprog.leaflet.lags.core.service.account.impl;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.mailing.domain.PasswordResetRequest;
import hu.psprog.leaflet.lags.core.domain.request.PasswordResetRequestModel;
import hu.psprog.leaflet.lags.core.domain.internal.SecurityConstants;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.util.NotificationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
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

        Optional<User> userOptional = userDAO.findByEmail(passwordResetRequestModel.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            OAuthTokenRequest passwordResetTokenRequest = createPasswordResetTokenRequest();
            Map<String, Object> claims = createPasswordReclaimTokenClaims(user);
            OAuthTokenResponse reclaimToken = tokenHandler.generateToken(passwordResetTokenRequest, claims, passwordResetConfig.getTokenExpiration());
            sendPasswordResetRequestNotification(user, reclaimToken);
        } else {
            log.warn("User account identified by email [{}] does not exist", passwordResetRequestModel.getEmail());
        }

        return null;
    }

    private OAuthTokenRequest createPasswordResetTokenRequest() {

        return OAuthTokenRequest.builder()
                .audience(passwordResetConfig.getAudience())
                .build();
    }

    private HashMap<String, Object> createPasswordReclaimTokenClaims(User user) {

        return new HashMap<>(Map.of(
                OAuthConstants.Token.SCOPE, SecurityConstants.RECLAIM_AUTHORITY.getAuthority(),
                OAuthConstants.Token.SUBJECT, String.format("password-reset|uid=%s", user.getId()),
                OAuthConstants.Token.ROLE, SecurityConstants.RECLAIM_ROLE,
                OAuthConstants.Token.USER, user.getEmail(),
                OAuthConstants.Token.NAME, user.getUsername(),
                OAuthConstants.Token.USER_ID, user.getId()
        ));
    }

    private void sendPasswordResetRequestNotification(User user, OAuthTokenResponse reclaimToken) {

        notificationAdapter.passwordResetRequested(PasswordResetRequest.builder()
                .username(user.getUsername())
                .participant(user.getEmail())
                .token(reclaimToken.getAccessToken())
                .expiration(reclaimToken.getExpiresIn())
                .build());
    }
}
