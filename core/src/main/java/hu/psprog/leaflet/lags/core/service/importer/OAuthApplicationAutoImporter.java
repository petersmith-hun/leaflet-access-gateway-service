package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * OAuth application registration importer. Reads the legacy application registrations are stores them in the database.
 * Import is skipped if there are already existing application registrations in the database (considering that an import
 * has been already successfully executed before). Import can also be disabled by setting the {@code oauth2-config.auto-import}
 * configuration parameter to {@code false}.
 *
 * @author Peter Smith
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "oauth2-config.auto-import", havingValue = "true")
public class OAuthApplicationAutoImporter {

    private final OAuthApplicationDAO oauthApplicationDAO;
    private final PermissionImporter permissionImporter;
    private final ClientImporter clientImporter;

    @Autowired
    OAuthApplicationAutoImporter(OAuthApplicationDAO oauthApplicationDAO, PermissionImporter permissionImporter,
                                 ClientImporter clientImporter) {

        this.oauthApplicationDAO = oauthApplicationDAO;
        this.permissionImporter = permissionImporter;
        this.clientImporter = clientImporter;
    }

    @PostConstruct
    public void init() {

        if (!allowImport()) {
            log.warn("OAuth application repository has already been initialized, skipping import");
            return;
        }

        permissionImporter.importPermissions();
        clientImporter.importClients();

        log.warn("Initial OAuth configuration import is done, auto-import should be disabled before the next start");
    }

    private boolean allowImport() {
        return oauthApplicationDAO.count() == 0;
    }
}
