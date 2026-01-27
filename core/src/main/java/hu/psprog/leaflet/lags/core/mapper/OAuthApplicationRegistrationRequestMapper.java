package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Mapper implementation for {@link OAuthApplicationRegistrationRequest}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthApplicationRegistrationRequestMapper {

    /**
     * Maps the given {@link OAuthApplicationRegistrationRequest} to {@link OAuthApplication} to be saved directly in the database.
     *
     * @param request source {@link OAuthApplicationRegistrationRequest} object
     * @return mapped {@link OAuthApplication} object
     */
    public OAuthApplication mapApplication(OAuthApplicationRegistrationRequest request) {

        return OAuthApplication.builder()
                .name(request.name())
                .clientId(request.clientID())
                .applicationType(request.registrationType().getApplicationType())
                .enabled(true)

                .callbacks(safeExtract(request.client(),
                        OAuthApplicationRegistrationRequest.ClientApplication::allowedCallbacks, this::mapCallback))
                .requiredPermissions(safeExtract(request.client(),
                        OAuthApplicationRegistrationRequest.ClientApplication::requiredPermissions, this::mapPermission))

                .audience(Optional.ofNullable(request.resourceServer())
                        .map(OAuthApplicationRegistrationRequest.ResourceServerApplication::audience)
                        .orElse(null))
                .registeredPermissions(safeExtract(request.resourceServer(),
                        OAuthApplicationRegistrationRequest.ResourceServerApplication::registeredPermissions, this::mapPermission))
                .allowedClients(safeExtract(request.resourceServer(),
                        OAuthApplicationRegistrationRequest.ResourceServerApplication::allowedClients, this::mapClient))

                .build();
    }

    private <S, I, T> List<T> safeExtract(S source, Function<S, List<I>> intermediateExtractor, Function<I, T> targetMapper) {

        return Optional.ofNullable(source)
                .map(intermediateExtractor)
                .map(itemList -> itemList.stream()
                        .map(targetMapper)
                        .toList())
                .orElse(null);

    }

    private OAuthCallback mapCallback(OAuthApplicationRegistrationRequest.ClientApplication.AllowedCallback callback) {

        return OAuthCallback.builder()
                .id(callback.id())
                .url(callback.url())
                .build();
    }

    private Permission mapPermission(UUID permissionID) {

        return Permission.builder()
                .id(permissionID)
                .build();
    }

    private OAuthAllowedClient mapClient(OAuthApplicationRegistrationRequest.ResourceServerApplication.AllowedClient client) {

        return OAuthAllowedClient.builder()
                .permissions(safeExtract(client.allowedPermissions(), Function.identity(), this::mapPermission))
                .targetApplication(OAuthApplication.builder()
                        .id(client.applicationID())
                        .build())
                .build();
    }
}
