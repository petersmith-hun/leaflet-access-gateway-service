package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for {@link OAuthApplicationRegistrationRequest}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationRegistrationRequestMapperTest {

    private static final String NAME_APP1 = "app1";
    private static final String CLIENT_ID_APP1 = "app-client-id-1";
    private static final String CALLBACK_1 = "http://localhost:9080";
    private static final UUID PERMISSION_1 = UUID.randomUUID();
    private static final UUID PERMISSION_2 = UUID.randomUUID();
    private static final UUID PERMISSION_3 = UUID.randomUUID();
    private static final UUID CALLBACK_ID = UUID.randomUUID();
    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final String AUDIENCE = "audience";

    @InjectMocks
    private OAuthApplicationRegistrationRequestMapper oAuthApplicationRegistrationRequestMapper;

    @Test
    public void shouldMapNewClientApplication() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .registrationType(OAuthApplicationRegistrationRequest.RegistrationType.CLIENT)
                .client(OAuthApplicationRegistrationRequest.ClientApplication.builder()
                        .allowedCallbacks(List.of(
                                prepareAllowedCallback(null)
                        ))
                        .requiredPermissions(List.of(PERMISSION_1, PERMISSION_2))
                        .build())
                .build();

        var expected = OAuthApplication.builder()
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.UI)
                .callbacks(List.of(prepareCallback(null)))
                .requiredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .enabled(true)
                .build();

        // when
        var result = oAuthApplicationRegistrationRequestMapper.mapApplication(request);

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapEditedClientApplication() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .registrationType(OAuthApplicationRegistrationRequest.RegistrationType.CLIENT)
                .client(OAuthApplicationRegistrationRequest.ClientApplication.builder()
                        .allowedCallbacks(List.of(
                                prepareAllowedCallback(CALLBACK_ID)
                        ))
                        .requiredPermissions(List.of(PERMISSION_1, PERMISSION_2))
                        .build())
                .build();

        var expected = OAuthApplication.builder()
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.UI)
                .callbacks(List.of(
                        prepareCallback(CALLBACK_ID)
                ))
                .requiredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .enabled(true)
                .build();

        // when
        var result = oAuthApplicationRegistrationRequestMapper.mapApplication(request);

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapResourceServerApplication() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .registrationType(OAuthApplicationRegistrationRequest.RegistrationType.RESOURCE_SERVER)
                .resourceServer(OAuthApplicationRegistrationRequest.ResourceServerApplication.builder()
                        .audience(AUDIENCE)
                        .registeredPermissions(List.of(PERMISSION_1, PERMISSION_2))
                        .allowedClients(List.of(
                                prepareAllowedClientRequest()
                        ))
                        .registeredPermissions(List.of(PERMISSION_1, PERMISSION_2, PERMISSION_3))
                        .build())
                .build();

        var expected = OAuthApplication.builder()
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .audience(AUDIENCE)
                .registeredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2),
                        preparePermission(PERMISSION_3)
                ))
                .allowedClients(List.of(
                        prepareAllowedClient()
                ))
                .enabled(true)
                .build();

        // when
        var result = oAuthApplicationRegistrationRequestMapper.mapApplication(request);

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapMiddleResourceServerApplication() {

        // given
        var request = OAuthApplicationRegistrationRequest.builder()
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .registrationType(OAuthApplicationRegistrationRequest.RegistrationType.MIDDLE_RESOURCE_SERVER)
                .client(OAuthApplicationRegistrationRequest.ClientApplication.builder()
                        .allowedCallbacks(List.of(
                                prepareAllowedCallback(CALLBACK_ID)
                        ))
                        .requiredPermissions(List.of(PERMISSION_1, PERMISSION_2))
                        .build())
                .resourceServer(OAuthApplicationRegistrationRequest.ResourceServerApplication.builder()
                        .audience(AUDIENCE)
                        .registeredPermissions(List.of(PERMISSION_1, PERMISSION_2))
                        .allowedClients(List.of(
                                prepareAllowedClientRequest()
                        ))
                        .registeredPermissions(List.of(PERMISSION_1, PERMISSION_2, PERMISSION_3))
                        .build())
                .build();

        var expected = OAuthApplication.builder()
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .audience(AUDIENCE)
                .callbacks(List.of(
                        prepareCallback(CALLBACK_ID)
                ))
                .requiredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .registeredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2),
                        preparePermission(PERMISSION_3)
                ))
                .allowedClients(List.of(
                        prepareAllowedClient()
                ))
                .enabled(true)
                .build();

        // when
        var result = oAuthApplicationRegistrationRequestMapper.mapApplication(request);

        // then
        assertThat(result, equalTo(expected));
    }

    private OAuthApplicationRegistrationRequest.ClientApplication.AllowedCallback prepareAllowedCallback(UUID id) {

        return OAuthApplicationRegistrationRequest.ClientApplication.AllowedCallback.builder()
                .id(id)
                .url(CALLBACK_1)
                .build();
    }

    private OAuthCallback prepareCallback(UUID id) {

        return OAuthCallback.builder()
                .id(id)
                .url(CALLBACK_1)
                .build();
    }

    private Permission preparePermission(UUID id) {

        return Permission.builder()
                .id(id)
                .build();
    }

    private OAuthApplicationRegistrationRequest.ResourceServerApplication.AllowedClient prepareAllowedClientRequest() {

        return OAuthApplicationRegistrationRequest.ResourceServerApplication.AllowedClient.builder()
                .applicationID(APPLICATION_ID)
                .allowedPermissions(List.of(PERMISSION_2))
                .build();
    }

    private OAuthAllowedClient prepareAllowedClient() {

        return OAuthAllowedClient.builder()
                .targetApplication(OAuthApplication.builder()
                        .id(APPLICATION_ID)
                        .build())
                .permissions(List.of(
                        preparePermission(PERMISSION_2)
                ))
                .build();
    }
}
