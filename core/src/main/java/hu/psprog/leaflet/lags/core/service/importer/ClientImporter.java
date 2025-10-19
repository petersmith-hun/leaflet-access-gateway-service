package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.service.factory.OAuthApplicationFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Imports the legacy OAuth applications. Before the import, it determines the dependency graph of the registrations,
 * making sure that they are imported in the right order, having those imported first being a dependency for other
 * registrations.
 *
 * @author Peter Smith
 */
@Slf4j
@Component
class ClientImporter {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;
    private final OAuthApplicationFactory oauthApplicationFactory;
    private final OAuthApplicationDAO oauthApplicationDAO;
    private final TopologicalSortUtility topologicalSortUtility;

    @Autowired
    ClientImporter(OAuthConfigurationProperties oAuthConfigurationProperties, OAuthApplicationFactory oauthApplicationFactory,
                   OAuthApplicationDAO oauthApplicationDAO, TopologicalSortUtility topologicalSortUtility) {

        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        this.oauthApplicationFactory = oauthApplicationFactory;
        this.oauthApplicationDAO = oauthApplicationDAO;
        this.topologicalSortUtility = topologicalSortUtility;
    }

    /**
     * Executes importing the OAuth application registrations.
     */
    @Transactional
    void importClients() {

        List<String> registrationNamesInTopologicalOrder = topologicalSortUtility.topologicalSortClients();

        registrationNamesInTopologicalOrder.stream()
                .map(this::findLegacyClientRegistrationByName)
                .peek(oAuthClient -> log.info("Importing OAuth application {}", oAuthClient.getClientId()))
                .map(oauthApplicationFactory::create)
                .forEach(oauthApplicationDAO::save);

        log.info("Successfully imported {} OAuth application registrations", registrationNamesInTopologicalOrder.size());
    }

    private OAuthClient findLegacyClientRegistrationByName(String clientName) {

        return oAuthConfigurationProperties.getClients()
                .stream()
                .filter(oAuthClient -> oAuthClient.getClientName().equals(clientName))
                .findFirst()
                .orElseThrow();
    }
}
