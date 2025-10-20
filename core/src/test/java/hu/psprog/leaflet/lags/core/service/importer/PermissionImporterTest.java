package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PermissionImporter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionImporterTest {

    @Mock
    private OAuthConfigurationProperties oAuthConfigurationProperties;

    @Mock
    private PermissionDAO permissionDAO;

    @InjectMocks
    private PermissionImporter permissionImporter;

    @Test
    public void shouldImportPermissions() {

        // given
        var expectedPermissionSet = Set.of(
                permission("permission1"),
                permission("permission2"),
                permission("permission3"),
                permission("permission4")
        );

        given(oAuthConfigurationProperties.getClients()).willReturn(List.of(
                client(
                        List.of("permission1", "permission2", "permission3"),
                        List.of(),
                        List.of(
                                relation("permission1"),
                                relation("permission3", "permission2")
                        )
                ),
                client(
                        List.of("permission4"),
                        List.of(),
                        List.of(
                                relation("permission4"),
                                relation("permission4"),
                                relation("permission4")
                        )
                ),
                client(
                        List.of(),
                        List.of("permission3", "permission4"),
                        List.of()
                )
        ));

        // when
        permissionImporter.importPermissions();

        // then
        verify(permissionDAO).saveAll(expectedPermissionSet);
    }

    private OAuthClient client(List<String> registeredPermissions, List<String> requiredPermissions,
                               List<OAuthClientAllowRelation> allowedClients) {

        return OAuthClient.builder()
                .allowedClients(allowedClients)
                .registeredScopes(requiredPermissions)
                .registeredScopes(registeredPermissions)
                .build();
    }

    private OAuthClientAllowRelation relation(String... permissions) {

        return OAuthClientAllowRelation.builder()
                .allowedScopes(List.of(permissions))
                .build();
    }

    private Permission permission(String name) {

        return Permission.builder()
                .name(name)
                .build();
    }
}
