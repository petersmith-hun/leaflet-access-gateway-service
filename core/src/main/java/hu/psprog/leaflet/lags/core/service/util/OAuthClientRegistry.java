package hu.psprog.leaflet.lags.core.service.util;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;

import java.util.Optional;

/**
 * Registry component implementation for OAuth2 client registrations.
 *
 * @author Peter Smith
 */
public interface OAuthClientRegistry {

    /**
     * Retrieves a registered OAuth2 client by its client ID.
     *
     * @param clientID client ID of the OAuth2 client
     * @return registered {@link OAuthClient} wrapped as {@link Optional}, or empty Optional if not found
     */
    Optional<OAuthClient> getClientByClientID(String clientID);

    /**
     * Retrieves a registered OAuth2 client by its audience.
     *
     * @param audience audience of the OAuth2 client
     * @return registered {@link OAuthClient} wrapped as {@link Optional}, or empty Optional if not found
     */
    Optional<OAuthClient> getClientByAudience(String audience);
}
