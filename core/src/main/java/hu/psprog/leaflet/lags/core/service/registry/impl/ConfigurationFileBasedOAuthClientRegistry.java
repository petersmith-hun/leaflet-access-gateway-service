package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.service.registry.OAuthClientRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@link OAuthClientRegistry} implementation using a Spring Boot application configuration file as its data source.
 *
 * @author Peter Smith
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oauth2-config.enable-legacy-registration", havingValue = "true")
public class ConfigurationFileBasedOAuthClientRegistry implements OAuthClientRegistry {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    @Autowired
    public ConfigurationFileBasedOAuthClientRegistry(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        log.warn("Application is running is legacy registration mode, please consider switching over to dynamic registration mode");
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
                .filter(oAuthClient -> identifier.equals(clientParameterMapperFunction.apply(oAuthClient)))
                .findFirst();
    }
}
