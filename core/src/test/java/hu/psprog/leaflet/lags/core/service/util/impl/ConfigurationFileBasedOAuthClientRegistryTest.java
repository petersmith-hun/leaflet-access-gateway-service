package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthConfigurationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link ConfigurationFileBasedOAuthClientRegistry}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ConfigurationFileBasedOAuthClientRegistryTest {

    private static final OAuthClient O_AUTH_CLIENT_1 = prepareOAuthClient("client-name-1", "client1", "audience-1");
    private static final OAuthClient O_AUTH_CLIENT_2 = prepareOAuthClient("client-name-2", "client2", "audience-2");
    private static final OAuthConfigurationProperties O_AUTH_CONFIGURATION_PROPERTIES = prepareOAuthConfigurationProperties();

    private ConfigurationFileBasedOAuthClientRegistry configurationFileBasedOAuthClientRegistry;

    @BeforeEach
    public void setup() {
        configurationFileBasedOAuthClientRegistry = new ConfigurationFileBasedOAuthClientRegistry(O_AUTH_CONFIGURATION_PROPERTIES);
    }

    @Test
    public void shouldGetClientByClientIDReturnRegisteredClient() {

        // when
        Optional<OAuthClient> result = configurationFileBasedOAuthClientRegistry.getClientByClientID(O_AUTH_CLIENT_1.getClientId());

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(O_AUTH_CLIENT_1));
    }

    @Test
    public void shouldGetClientByClientIDReturnEmptyOptionalForNonRegisteredClient() {

        // when
        Optional<OAuthClient> result = configurationFileBasedOAuthClientRegistry.getClientByClientID("non-registered-client-id");

        // then
        assertThat(result.isPresent(), is(false));
    }

    @Test
    public void shouldGetClientByAudienceReturnRegisteredClient() {

        // when
        Optional<OAuthClient> result = configurationFileBasedOAuthClientRegistry.getClientByAudience(O_AUTH_CLIENT_2.getAudience());

        // then
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), equalTo(O_AUTH_CLIENT_2));
    }

    @Test
    public void shouldGetClientByAudienceReturnEmptyOptionalForNonRegisteredClient() {

        // when
        Optional<OAuthClient> result = configurationFileBasedOAuthClientRegistry.getClientByAudience("non-registered-audience");

        // then
        assertThat(result.isPresent(), is(false));
    }

    private static OAuthClient prepareOAuthClient(String clientName, String clientId, String audience) {
        return new OAuthClient(clientName, clientId, null, audience, Collections.emptyList(), Collections.emptyList());
    }

    private static OAuthConfigurationProperties prepareOAuthConfigurationProperties() {
        return new OAuthConfigurationProperties(null, Arrays.asList(O_AUTH_CLIENT_1, O_AUTH_CLIENT_2));
    }
}
