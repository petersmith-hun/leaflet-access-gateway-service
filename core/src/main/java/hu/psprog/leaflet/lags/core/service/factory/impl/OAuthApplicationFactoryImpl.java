package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.exception.OAuthApplicationImportException;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import hu.psprog.leaflet.lags.core.service.factory.OAuthApplicationFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link OAuthApplicationFactory}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthApplicationFactoryImpl implements OAuthApplicationFactory {

    private final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[abxy]\\$[0-9]{1,2}\\$.*");

    private final OAuthApplicationDAO oAuthApplicationDAO;
    private final PermissionDAO permissionDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public OAuthApplicationFactoryImpl(OAuthApplicationDAO oAuthApplicationDAO, PermissionDAO permissionDAO,
                                       PasswordEncoder passwordEncoder) {

        this.oAuthApplicationDAO = oAuthApplicationDAO;
        this.permissionDAO = permissionDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuthApplication create(OAuthClient oAuthClient) {

        return OAuthApplication.builder()
                .name(oAuthClient.getClientName())
                .applicationType(oAuthClient.getApplicationType())
                .clientId(oAuthClient.getClientId())
                .clientSecret(encryptSecretIfNeeded(oAuthClient))
                .audience(oAuthClient.getAudience())
                .callbacks(mapCallbacks(oAuthClient))
                .allowedClients(mapAllowedClients(oAuthClient))
                .requiredPermissions(mapPermissions(oAuthClient.getRequiredScopes()))
                .registeredPermissions(mapPermissions(oAuthClient.getRegisteredScopes()))
                .enabled(true)
                .build();
    }

    private String encryptSecretIfNeeded(OAuthClient oAuthClient) {

        return Optional.ofNullable(oAuthClient.getClientSecret())
                .filter(Predicate.not(StringUtils::isEmpty))
                .filter(clientSecret -> !BCRYPT_PATTERN.matcher(oAuthClient.getClientSecret()).matches())
                .map(passwordEncoder::encode)
                .orElse(oAuthClient.getClientSecret());
    }

    private List<OAuthCallback> mapCallbacks(OAuthClient oAuthClient) {

        return oAuthClient.getAllowedCallbacks()
                .stream()
                .map(callbackURL -> OAuthCallback.builder()
                        .url(callbackURL)
                        .build())
                .toList();
    }

    private List<OAuthAllowedClient> mapAllowedClients(OAuthClient oAuthClient) {

        return oAuthClient.getAllowedClients()
                .stream()
                .map(relation -> OAuthAllowedClient.builder()
                        .targetApplication(oAuthApplicationDAO.findByName(relation.getName())
                                .orElseThrow(() -> new OAuthApplicationImportException("OAuth application registration by name %s is not found".formatted(relation.getName()))))
                        .permissions(mapPermissions(relation.getAllowedScopes()))
                        .build())
                .toList();
    }

    private List<Permission> mapPermissions(List<String> scopes) {

        List<Permission> assignedPermissions = permissionDAO.findAllByNames(scopes);
        if (scopes.size() != assignedPermissions.size()) {
            throw new OAuthApplicationImportException("Permission count mismatch (requested %d, found %d)".formatted(scopes.size(), assignedPermissions.size()));
        }

        return assignedPermissions;
    }
}
