package hu.psprog.leaflet.lags.core.service.factory;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;

/**
 * Factory component for creating {@link OAuthApplication} objects.
 *
 * @author Peter Smith
 */
public interface OAuthApplicationFactory {

    /**
     * Creates an {@link OAuthApplication} object based on the given {@link OAuthClient}. Implementation ensures that
     * referenced allowed clients already exist in the database, as well as the defined permissions. Also checks if any
     * given OAuth client secret is already encrypted, otherwise encrypts it before conversion.
     *
     * @param oAuthClient source {@link OAuthClient} object (coming from the legacy or the dynamic configuration API)
     * @return created {@link OAuthApplication} object that can be directly stored in the database
     */
    OAuthApplication create(OAuthClient oAuthClient);
}
