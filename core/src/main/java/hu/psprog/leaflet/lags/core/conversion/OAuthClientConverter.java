package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

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

        return source.getAllowedClients()
                .stream()
                .map(this::mapRelation)
                .toList();
    }

    private OAuthClientAllowRelation mapRelation(OAuthAllowedClient client) {

        return OAuthClientAllowRelation.builder()
                .name(client.getTargetApplication().getName())
                .allowedScopes(mapPermissions(client.getPermissions()))
                .build();
    }

    private List<String> mapCallbacks(OAuthApplication source) {

        return source.getCallbacks()
                .stream()
                .map(OAuthCallback::getUrl)
                .toList();
    }

    private List<String> mapPermissions(List<Permission> permissions) {

        return permissions.stream()
                .map(Permission::getName)
                .toList();
    }
}
