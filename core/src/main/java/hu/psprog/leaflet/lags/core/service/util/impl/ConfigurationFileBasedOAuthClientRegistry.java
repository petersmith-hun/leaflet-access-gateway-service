package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@link OAuthClientRegistry} implementation using a Spring Boot application configuration file as its data source.
 *
 * @author Peter Smith
 */
@Component
public class ConfigurationFileBasedOAuthClientRegistry implements OAuthClientRegistry {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    @Autowired
    public ConfigurationFileBasedOAuthClientRegistry(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    @Override
    public Optional<OAuthClient> getClientByClientID(String clientID) {
        return getClient(OAuthClient::getClientId, clientID);
    }

    @Override
    public Optional<OAuthClient> getClientByAudience(String audience) {
        return getClient(OAuthClient::getAudience, audience);
    }

    private Optional<OAuthClient> getClient(Function<OAuthClient, String> clientParameterMapperFunction, String identifier) {

        return oAuthConfigurationProperties.getClients().stream()
                .filter(oAuthClient -> clientParameterMapperFunction.apply(oAuthClient).equals(identifier))
                .findFirst();
    }
}
