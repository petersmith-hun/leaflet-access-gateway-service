package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Mapper implementation for {@link OAuthApplication} entity.
 *
 * @author Peter Smith
 */
@Component
public class OAuthApplicationMapper extends AbstractCommonMapper {

    /**
     * Maps the given {@link OAuthApplication} to {@link OAuthApplicationSummaryResponse}.
     *
     * @param application source {@link OAuthApplication} object
     * @return mapped {@link OAuthApplicationSummaryResponse} object
     */
    public OAuthApplicationSummaryResponse mapApplicationSummary(OAuthApplication application) {

        return OAuthApplicationSummaryResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .clientID(application.getClientId())
                .applicationType(application.getApplicationType())
                .enabled(application.isEnabled())
                .created(convertDate(application.getCreatedAt()))
                .lastModified(convertDate(application.getUpdatedAt()))
                .build();
    }

    /**
     * Maps the given {@link OAuthApplication} to {@link OAuthApplicationResponse}, including the related resource servers (if any).
     *
     * @param application source {@link OAuthApplication} object
     * @param resourceServersOfClient list of {@link OAuthApplication} entities contacted as a resource server by this application
     * @return mapped {@link OAuthApplicationSummaryResponse} object
     */
    public OAuthApplicationResponse mapApplication(OAuthApplication application, List<OAuthApplication> resourceServersOfClient) {

        return OAuthApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .clientID(application.getClientId())
                .applicationType(application.getApplicationType())
                .client(isConsideredClient(application, resourceServersOfClient)
                        ? mapClient(application, resourceServersOfClient)
                        : null)
                .resourceServer(application.getApplicationType() == ApplicationType.SERVICE
                        ? mapResourceServer(application)
                        : null)
                .enabled(application.isEnabled())
                .created(convertDate(application.getCreatedAt()))
                .lastModified(convertDate(application.getUpdatedAt()))
                .build();
    }

    private boolean isConsideredClient(OAuthApplication application, List<OAuthApplication> resourceServersOfClient) {

        return application.getApplicationType() == ApplicationType.UI
                || hasRequiredPermissions(application)
                || !resourceServersOfClient.isEmpty();
    }

    private boolean hasRequiredPermissions(OAuthApplication application) {

        return Optional.ofNullable(application.getRequiredPermissions())
                .map(permissions -> !permissions.isEmpty())
                .orElse(false);
    }

    private OAuthApplicationResponse.ClientApplication mapClient(OAuthApplication application, List<OAuthApplication> resourceServersOfClient) {

        return OAuthApplicationResponse.ClientApplication.builder()
                .allowedCallbacks(mapCallbacks(application.getCallbacks()))
                .requiredPermissions(mapPermissions(application.getRequiredPermissions()))
                .resourceServers(resourceServersOfClient.stream()
                        .map(resourceServerApplication -> OAuthApplicationResponse.TargetApplication.builder()
                                .id(resourceServerApplication.getId())
                                .name(resourceServerApplication.getName())
                                .clientID(resourceServerApplication.getClientId())
                                .build())
                        .toList())
                .build();
    }

    private OAuthApplicationResponse.ResourceServerApplication.AllowedClient mapAllowedClient(OAuthAllowedClient client) {

        return OAuthApplicationResponse.ResourceServerApplication.AllowedClient.builder()
                .id(client.getId())
                .application(OAuthApplicationResponse.TargetApplication.builder()
                        .id(client.getTargetApplication().getId())
                        .name(client.getTargetApplication().getName())
                        .clientID(client.getTargetApplication().getClientId())
                        .build())
                .allowedPermissions(mapPermissions(client.getPermissions()))
                .build();
    }

    private OAuthApplicationResponse.ResourceServerApplication mapResourceServer(OAuthApplication application) {

        return OAuthApplicationResponse.ResourceServerApplication.builder()
                .audience(application.getAudience())
                .registeredPermissions(mapPermissions(application.getRegisteredPermissions()))
                .allowedClients(application.getAllowedClients()
                        .stream()
                        .map(this::mapAllowedClient)
                        .toList())
                .build();
    }

    private List<OAuthApplicationResponse.ClientApplication.Callback> mapCallbacks(List<OAuthCallback> callbacks) {

        if (callbacks == null) {
            return Collections.emptyList();
        }

        return callbacks.stream()
                .map(callback -> OAuthApplicationResponse.ClientApplication.Callback.builder()
                        .id(callback.getId())
                        .url(callback.getUrl())
                        .build())
                .toList();
    }

    private List<OAuthApplicationResponse.Permission> mapPermissions(List<Permission> permissions) {

        if (permissions == null) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(permission -> OAuthApplicationResponse.Permission.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build())
                .toList();
    }

}
