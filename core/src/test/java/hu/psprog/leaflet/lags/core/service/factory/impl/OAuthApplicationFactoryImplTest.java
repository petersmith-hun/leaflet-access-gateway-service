package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.exception.OAuthApplicationImportException;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link OAuthApplicationFactoryImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationFactoryImplTest {

    @Mock
    private OAuthApplicationDAO oAuthApplicationDAO;

    @Mock
    private PermissionDAO permissionDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OAuthApplicationFactoryImpl oAuthApplicationFactory;

    @Test
    public void shouldCreateApplicationForService() {

        // given
        var client = OAuthClient.builder()
                .clientName("service-1")
                .applicationType(ApplicationType.SERVICE)
                .clientId("client-id-1")
                .audience("audience-1")
                .allowedClients(List.of(
                        relation("ui-1", "permission-1", "permission-2"),
                        relation("ui-2", "permission-2")
                ))
                .registeredScopes(List.of("permission-1", "permission-2", "permission-3"))
                .build();

        var targetApplication1 = targetApplication("ui-1");
        var targetApplication2 = targetApplication("ui-2");

        var permission1 = permission("permission-1");
        var permission2 = permission("permission-2");
        var permission3 = permission("permission-3");

        var allowedClient1 = client(targetApplication1, permission1, permission2);
        var allowedClient2 = client(targetApplication2, permission2);

        var expectedResult = OAuthApplication.builder()
                .name("service-1")
                .applicationType(ApplicationType.SERVICE)
                .clientId("client-id-1")
                .clientSecret(null)
                .audience("audience-1")
                .allowedClients(List.of(allowedClient1, allowedClient2))
                .registeredPermissions(List.of(permission1, permission2, permission3))
                .requiredPermissions(Collections.emptyList())
                .callbacks(Collections.emptyList())
                .enabled(true)
                .build();

        given(oAuthApplicationDAO.findByName("ui-1")).willReturn(Optional.of(targetApplication1));
        given(oAuthApplicationDAO.findByName("ui-2")).willReturn(Optional.of(targetApplication2));

        given(permissionDAO.findAllByNames(client.getAllowedClients().get(0).getAllowedScopes()))
                .willReturn(List.of(permission1, permission2));
        given(permissionDAO.findAllByNames(client.getAllowedClients().get(1).getAllowedScopes()))
                .willReturn(List.of(permission2));
        given(permissionDAO.findAllByNames(client.getRegisteredScopes()))
                .willReturn(List.of(permission1, permission2, permission3));
        given(permissionDAO.findAllByNames(Collections.emptyList()))
                .willReturn(Collections.emptyList());

        // when
        var result = oAuthApplicationFactory.create(client);

        // then
        assertThat(result, equalTo(expectedResult));

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    public void shouldCreateApplicationForUIWithUnencryptedSecret() {

        // given
        var client = OAuthClient.builder()
                .clientName("ui-1")
                .applicationType(ApplicationType.UI)
                .clientId("client-id-1")
                .clientSecret("client-secret-1")
                .requiredScopes(List.of("permission-1", "permission-2", "permission-3"))
                .allowedCallbacks(List.of("callback-1", "callback-2"))
                .build();

        var callback1 = callback("callback-1");
        var callback2 = callback("callback-2");

        var permission1 = permission("permission-1");
        var permission2 = permission("permission-2");
        var permission3 = permission("permission-3");

        var encryptedClientSecret = "encrypted-secret-1";

        var expectedResult = OAuthApplication.builder()
                .name("ui-1")
                .applicationType(ApplicationType.UI)
                .clientId("client-id-1")
                .clientSecret(encryptedClientSecret)
                .audience(null)
                .allowedClients(Collections.emptyList())
                .registeredPermissions(Collections.emptyList())
                .requiredPermissions(List.of(permission1, permission2, permission3))
                .callbacks(List.of(callback1, callback2))
                .enabled(true)
                .build();

        given(permissionDAO.findAllByNames(client.getRequiredScopes()))
                .willReturn(List.of(permission1, permission2, permission3));
        given(permissionDAO.findAllByNames(Collections.emptyList()))
                .willReturn(Collections.emptyList());
        given(passwordEncoder.encode(client.getClientSecret())).willReturn(encryptedClientSecret);

        // when
        var result = oAuthApplicationFactory.create(client);

        // then
        assertThat(result, equalTo(expectedResult));

        verifyNoInteractions(oAuthApplicationDAO);
    }

    @Test
    public void shouldCreateApplicationForUIWithEncryptedSecret() {

        // given
        var encryptedClientSecret = "$2a$10$...client-secret-1";

        var client = OAuthClient.builder()
                .clientName("ui-2")
                .applicationType(ApplicationType.UI)
                .clientId("client-id-2")
                .clientSecret(encryptedClientSecret)
                .requiredScopes(List.of("permission-4"))
                .allowedCallbacks(List.of("callback-1"))
                .build();

        var callback1 = callback("callback-1");

        var permission4 = permission("permission-4");

        var expectedResult = OAuthApplication.builder()
                .name("ui-2")
                .applicationType(ApplicationType.UI)
                .clientId("client-id-2")
                .clientSecret(encryptedClientSecret)
                .audience(null)
                .allowedClients(Collections.emptyList())
                .registeredPermissions(Collections.emptyList())
                .requiredPermissions(List.of(permission4))
                .callbacks(List.of(callback1))
                .enabled(true)
                .build();

        given(permissionDAO.findAllByNames(client.getRequiredScopes())).willReturn(List.of(permission4));
        given(permissionDAO.findAllByNames(Collections.emptyList())).willReturn(Collections.emptyList());

        // when
        var result = oAuthApplicationFactory.create(client);

        // then
        assertThat(result, equalTo(expectedResult));

        verifyNoInteractions(passwordEncoder, oAuthApplicationDAO);
    }

    @Test
    public void shouldCreateThrowExceptionOnPermissionCountMismatch() {

        // given
        var client = OAuthClient.builder()
                .clientName("ui-1")
                .applicationType(ApplicationType.UI)
                .clientId("client-id-1")
                .clientSecret("$2a$10$...client-secret-1")
                .requiredScopes(List.of("permission-1", "permission-2", "permission-3"))
                .allowedCallbacks(Collections.emptyList())
                .build();

        var permission1 = permission("permission-1");

        given(permissionDAO.findAllByNames(client.getRequiredScopes())).willReturn(List.of(permission1));

        // when
        var exception = assertThrows(OAuthApplicationImportException.class, () -> oAuthApplicationFactory.create(client));

        // then
        // exception expected
        assertThat(exception.getMessage(), equalTo("Permission count mismatch (requested 3, found 1)"));
    }

    @Test
    public void shouldCreateThrowExceptionOnMissingApplicationRegistration() {

        // given
        var client = OAuthClient.builder()
                .clientName("service-1")
                .applicationType(ApplicationType.SERVICE)
                .clientId("client-id-1")
                .audience("audience-1")
                .allowedClients(List.of(
                        relation("ui-1", "permission-2")
                ))
                .build();

        given(oAuthApplicationDAO.findByName("ui-1")).willReturn(Optional.empty());

        // when
        var exception = assertThrows(OAuthApplicationImportException.class, () -> oAuthApplicationFactory.create(client));

        // then
        // exception expected
        assertThat(exception.getMessage(), equalTo("OAuth application registration by name ui-1 is not found"));
    }

    private OAuthClientAllowRelation relation(String name, String... permissions) {

        return OAuthClientAllowRelation.builder()
                .name(name)
                .allowedScopes(List.of(permissions))
                .build();
    }

    private OAuthApplication targetApplication(String name) {

        return OAuthApplication.builder()
                .name(name)
                .build();
    }

    private Permission permission(String name) {

        return Permission.builder()
                .name(name)
                .build();
    }

    private OAuthAllowedClient client(OAuthApplication application, Permission... permissions) {

        return OAuthAllowedClient.builder()
                .targetApplication(application)
                .permissions(List.of(permissions))
                .build();
    }

    private OAuthCallback callback(String url) {

        return OAuthCallback.builder()
                .url(url)
                .build();
    }
}
