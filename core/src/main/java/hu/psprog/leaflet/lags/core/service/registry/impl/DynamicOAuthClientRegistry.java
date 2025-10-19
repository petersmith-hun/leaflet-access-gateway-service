package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.conversion.OAuthClientConverter;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.service.registry.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * {@link OAuthClientRegistry} implementation to handle OAuth application registrations stored in the database.
 *
 * @author Peter Smith
 */
@Component
@ConditionalOnProperty(name = "oauth2-config.enable-legacy-registration", havingValue = "false", matchIfMissing = true)
public class DynamicOAuthClientRegistry implements OAuthClientRegistry {

    private final OAuthApplicationDAO oAuthApplicationDAO;
    private final OAuthClientConverter oAuthClientConverter;

    @Autowired
    public DynamicOAuthClientRegistry(OAuthApplicationDAO oAuthApplicationDAO, OAuthClientConverter oAuthClientConverter) {
        this.oAuthApplicationDAO = oAuthApplicationDAO;
        this.oAuthClientConverter = oAuthClientConverter;
    }

    @Override
    public Optional<OAuthClient> getClientByClientID(String clientID) {

        return oAuthApplicationDAO.findByClientID(clientID)
                .map(oAuthClientConverter::convert);
    }

    @Override
    public Optional<OAuthClient> getClientByAudience(String audience) {

        return oAuthApplicationDAO.findByAudience(audience)
                .map(oAuthClientConverter::convert);
    }
}
