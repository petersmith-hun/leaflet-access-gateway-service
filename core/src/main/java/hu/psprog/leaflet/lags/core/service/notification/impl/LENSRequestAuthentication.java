package hu.psprog.leaflet.lags.core.service.notification.impl;

import hu.psprog.leaflet.bridge.client.request.RequestAuthentication;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.service.OAuthAuthorizationService;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bridge {@link RequestAuthentication} implementation for self-claiming access token.
 *
 * LAGS also needs to acquire an access token for LENS, however it's not possible to do a self HTTP call while the
 * application is still starting up. Therefore, this implementation is able to request the token internally, partially
 * bypassing the standard token claim flow, but still going through the necessary checks.
 *
 * The required configuration parameters can be set up via {@code bridge.clients.lens.token-request}, and similarly to
 * any standard OAuth client registration, client ID, audience and scope are required. Standard client authentication is
 * bypassed, so client secret is not needed.
 *
 * @author Peter Smith
 */
@Component
@Setter(AccessLevel.PACKAGE)
@ConfigurationProperties(prefix = "bridge.clients.lens.token-request")
@Slf4j
public class LENSRequestAuthentication implements RequestAuthentication {

    private final OAuthAuthorizationService oAuthAuthorizationService;

    private LENSToken currentToken = new LENSToken();
    private String clientID;
    private String audience;
    private List<String> scope;

    @Autowired
    public LENSRequestAuthentication(OAuthAuthorizationService oAuthAuthorizationService) {
        this.oAuthAuthorizationService = oAuthAuthorizationService;
    }

    @Override
    public Map<String, String> getAuthenticationHeader() {

        if (currentToken.needsRenewal()) {
            log.info("LENS access token is due renewal, generating new token...");
            currentToken = createLENSToken();
        }

        return currentToken.getHeader();
    }

    private LENSToken createLENSToken() {

        OAuthTokenRequest tokenRequest = OAuthTokenRequest.builder()
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .clientID(clientID)
                .audience(audience)
                .scope(scope)
                .build();

        return new LENSToken(oAuthAuthorizationService.authorize(tokenRequest));
    }

    /**
     * Internally used domain class for storing and managing the claimed LENS token.
     */
    static class LENSToken {

        private static final int TOKEN_EXPIRATION_THRESHOLD_IN_MINUTES = 5;
        private static final String AUTHORIZATION_HEADER = "Authorization";
        private static final String BEARER_TEMPLATE = "Bearer %s";

        private final LocalDateTime expiresAt;
        private final Map<String, String> header;

        private LENSToken() {
            this.expiresAt = null;
            this.header = null;
        }

        LENSToken(OAuthTokenResponse tokenResponse) {
            this.expiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn());
            this.header = Map.of(AUTHORIZATION_HEADER, String.format(BEARER_TEMPLATE, tokenResponse.getAccessToken()));
        }

        /**
         * Checks if the stored token is due renewal.
         * This can happen in two cases:
         *  - Token is not yet acquired (authorization header is not set);
         *  - Token expires within a 5-minute threshold (or is already expired).
         *
         * @return {@code true} if the stored access token is due renewal, {@code false} otherwise
         */
        boolean needsRenewal() {

            return Objects.isNull(header)
                    || Duration.between(LocalDateTime.now(), expiresAt).toMinutes() < TOKEN_EXPIRATION_THRESHOLD_IN_MINUTES;
        }

        /**
         * Returns the generated header value as a {@link Map} of {@link String} key-value pair(s).
         *
         * @return generated header as {@link Map}
         */
        Map<String, String> getHeader() {
            return header;
        }
    }
}
