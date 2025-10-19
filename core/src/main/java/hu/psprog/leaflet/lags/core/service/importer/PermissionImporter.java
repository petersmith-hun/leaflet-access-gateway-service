package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Imports permissions referenced by any OAuth application registration (in registered and required permission list, or
 * in allowed permissions for related clients).
 *
 * @author Peter Smith
 */
@Component
class PermissionImporter {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;
    private final PermissionDAO permissionDAO;

    @Autowired
    PermissionImporter(OAuthConfigurationProperties oAuthConfigurationProperties, PermissionDAO permissionDAO) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
        this.permissionDAO = permissionDAO;
    }

    /**
     * Executes importing the OAuth permissions referenced by OAuth application registrations.
     */
    @Transactional
    void importPermissions() {

        Set<Permission> distinctPermissions = collectDistinctPermissions()
                .sorted()
                .map(this::mapPermission)
                .collect(Collectors.toSet());

        permissionDAO.saveAll(distinctPermissions);
    }

    private Permission mapPermission(String scope) {

        return Permission.builder()
                .name(scope)
                .build();
    }

    private Stream<String> collectDistinctPermissions() {

        return oAuthConfigurationProperties.getClients()
                .stream()
                .flatMap(client -> Stream.concat(
                        client.getAllowedClients().stream()
                                .flatMap(relation -> relation.getAllowedScopes().stream()),
                        Stream.concat(client.getRegisteredScopes().stream(), client.getRequiredScopes().stream())
                ))
                .distinct();
    }
}
