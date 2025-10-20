package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Converts the given {@link OAuthApplication} object to {@link OAuthClient}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthClientConverter implements Converter<OAuthApplication, OAuthClient> {

    @Override
    public OAuthClient convert(OAuthApplication source) {

        return OAuthClient.builder()
                .applicationType(source.getApplicationType())
                .clientName(source.getName())
                .clientId(source.getClientId())
                .clientSecret(source.getClientSecret())
                .audience(source.getAudience())
                .allowedClients(mapClients(source))
                .allowedCallbacks(mapCallbacks(source))
                .registeredScopes(mapPermissions(source.getRegisteredPermissions()))
                .requiredScopes(mapPermissions(source.getRequiredPermissions()))
                .build();
    }

    private List<OAuthClientAllowRelation> mapClients(OAuthApplication source) {
        return safeMapList(source.getAllowedClients(), this::mapRelation);
    }

    private OAuthClientAllowRelation mapRelation(OAuthAllowedClient client) {

        return OAuthClientAllowRelation.builder()
                .name(client.getTargetApplication().getName())
                .allowedScopes(mapPermissions(client.getPermissions()))
                .build();
    }

    private List<String> mapCallbacks(OAuthApplication source) {
        return safeMapList(source.getCallbacks(), OAuthCallback::getUrl);
    }

    private List<String> mapPermissions(List<Permission> permissions) {
        return safeMapList(permissions, Permission::getName);
    }

    private <S, T> List<T> safeMapList(List<S> sourceList, Function<S, T> mapperFunction) {

        return Optional.ofNullable(sourceList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(mapperFunction)
                .toList();
    }
}
