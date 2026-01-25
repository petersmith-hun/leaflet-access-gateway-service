package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for {@link OAuthApplicationMapper}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationMapperTest {

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final String NAME_APP1 = "app1";
    private static final String CLIENT_ID_APP1 = "app-client-id-1";
    private static final String RELATED_NAME = "app2";
    private static final String RELATED_CLIENT_ID = "app-client-id-2";
    private static final UUID RELATED_APPLICATION_ID = UUID.randomUUID();
    private static final String CALLBACK_1 = "http://localhost:9080";
    private static final UUID PERMISSION_1 = UUID.randomUUID();
    private static final UUID PERMISSION_2 = UUID.randomUUID();
    private static final UUID PERMISSION_3 = UUID.randomUUID();
    private static final UUID CALLBACK_ID = UUID.randomUUID();
    private static final UUID ALLOWED_CLIENT_RELATION_ID = UUID.randomUUID();
    private static final String AUDIENCE = "audience";
    private static final Instant BASELINE_INSTANT_CREATED = Instant.ofEpochMilli(System.currentTimeMillis());
    private static final Instant BASELINE_INSTANT_UPDATED = BASELINE_INSTANT_CREATED.plusSeconds(60);

    @InjectMocks
    private OAuthApplicationMapper oAuthApplicationMapper;

    @Test
    public void shouldMapApplicationSummary() {

        // given
        var application = OAuthApplication.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .build();

        var expected = OAuthApplicationSummaryResponse.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .build();

        // when
        var result = oAuthApplicationMapper.mapApplicationSummary(application);

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapClientApplication() {

        // given
        var application = OAuthApplication.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.UI)
                .enabled(true)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .callbacks(List.of(
                        prepareCallback()
                ))
                .requiredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .build();

        var resourceServerOfClient = OAuthApplication.builder()
                .id(RELATED_APPLICATION_ID)
                .name(RELATED_NAME)
                .clientId(RELATED_CLIENT_ID)
                .build();

        var expectedTarget = OAuthApplicationResponse.TargetApplication.builder()
                .id(RELATED_APPLICATION_ID)
                .name(RELATED_NAME)
                .clientID(RELATED_CLIENT_ID)
                .build();

        var expected = OAuthApplicationResponse.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .applicationType(ApplicationType.UI)
                .enabled(true)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .client(OAuthApplicationResponse.ClientApplication.builder()
                        .allowedCallbacks(List.of(
                                prepareCallbackResponse()
                        ))
                        .requiredPermissions(List.of(
                                preparePermissionResponse(PERMISSION_1),
                                preparePermissionResponse(PERMISSION_2)
                        ))
                        .resourceServers(List.of(expectedTarget))
                        .build())
                .build();

        // when
        var result = oAuthApplicationMapper.mapApplication(application, List.of(resourceServerOfClient));

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapResourceServerApplication() {

        // given
        var application = OAuthApplication.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .audience(AUDIENCE)
                .registeredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .allowedClients(List.of(
                        prepareAllowedClient()
                ))
                .build();

        var expected = OAuthApplicationResponse.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .resourceServer(OAuthApplicationResponse.ResourceServerApplication.builder()
                        .audience(AUDIENCE)
                        .registeredPermissions(List.of(
                                preparePermissionResponse(PERMISSION_1),
                                preparePermissionResponse(PERMISSION_2)
                        ))
                        .allowedClients(List.of(
                                prepareAllowedClientResponse()
                        ))
                        .build())
                .build();

        // when
        var result = oAuthApplicationMapper.mapApplication(application, List.of());

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapMiddleResourceServerApplication() {

        // given
        var application = OAuthApplication.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientId(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .audience(AUDIENCE)
                .registeredPermissions(List.of(
                        preparePermission(PERMISSION_1),
                        preparePermission(PERMISSION_2)
                ))
                .requiredPermissions(List.of(
                        preparePermission(PERMISSION_3)
                ))
                .allowedClients(List.of(
                        prepareAllowedClient()
                ))
                .build();

        var resourceServerOfClient = OAuthApplication.builder()
                .id(RELATED_APPLICATION_ID)
                .name(RELATED_NAME)
                .clientId(RELATED_CLIENT_ID)
                .build();

        var expectedTarget = OAuthApplicationResponse.TargetApplication.builder()
                .id(RELATED_APPLICATION_ID)
                .name(RELATED_NAME)
                .clientID(RELATED_CLIENT_ID)
                .build();

        var expected = OAuthApplicationResponse.builder()
                .id(APPLICATION_ID)
                .name(NAME_APP1)
                .clientID(CLIENT_ID_APP1)
                .applicationType(ApplicationType.SERVICE)
                .enabled(true)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .client(OAuthApplicationResponse.ClientApplication.builder()
                        .allowedCallbacks(Collections.emptyList())
                        .resourceServers(List.of(expectedTarget))
                        .requiredPermissions(List.of(
                                preparePermissionResponse(PERMISSION_3)
                        ))
                        .build())
                .resourceServer(OAuthApplicationResponse.ResourceServerApplication.builder()
                        .audience(AUDIENCE)
                        .registeredPermissions(List.of(
                                preparePermissionResponse(PERMISSION_1),
                                preparePermissionResponse(PERMISSION_2)
                        ))
                        .allowedClients(List.of(
                                prepareAllowedClientResponse()
                        ))
                        .build())
                .build();

        // when
        var result = oAuthApplicationMapper.mapApplication(application, List.of(resourceServerOfClient));

        // then
        assertThat(result, equalTo(expected));
    }

    private OAuthCallback prepareCallback() {

        return OAuthCallback.builder()
                .id(CALLBACK_ID)
                .url(CALLBACK_1)
                .build();
    }

    private OAuthApplicationResponse.ClientApplication.Callback prepareCallbackResponse() {

        return OAuthApplicationResponse.ClientApplication.Callback.builder()
                .id(CALLBACK_ID)
                .url(CALLBACK_1)
                .build();
    }

    private Permission preparePermission(UUID id) {

        return Permission.builder()
                .id(id)
                .name("name_" + id)
                .description("description_" + id)
                .build();
    }

    private OAuthApplicationResponse.Permission preparePermissionResponse(UUID id) {

        return OAuthApplicationResponse.Permission.builder()
                .id(id)
                .name("name_" + id)
                .description("description_" + id)
                .build();
    }

    private OAuthAllowedClient prepareAllowedClient() {

        return OAuthAllowedClient.builder()
                .id(ALLOWED_CLIENT_RELATION_ID)
                .targetApplication(OAuthApplication.builder()
                        .id(RELATED_APPLICATION_ID)
                        .name(RELATED_NAME)
                        .clientId(RELATED_CLIENT_ID)
                        .build())
                .permissions(List.of(
                        preparePermission(PERMISSION_3)
                ))
                .build();
    }

    private OAuthApplicationResponse.ResourceServerApplication.AllowedClient prepareAllowedClientResponse() {

        return OAuthApplicationResponse.ResourceServerApplication.AllowedClient.builder()
                .id(ALLOWED_CLIENT_RELATION_ID)
                .application(OAuthApplicationResponse.TargetApplication.builder()
                        .id(RELATED_APPLICATION_ID)
                        .name(RELATED_NAME)
                        .clientID(RELATED_CLIENT_ID)
                        .build())
                .allowedPermissions(List.of(
                        preparePermissionResponse(PERMISSION_3)
                ))
                .build();
    }
}
