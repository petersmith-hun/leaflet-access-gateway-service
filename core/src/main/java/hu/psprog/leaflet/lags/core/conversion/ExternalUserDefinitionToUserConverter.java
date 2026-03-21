package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.exception.MissingDefaultRoleException;
import hu.psprog.leaflet.lags.core.persistence.dao.RoleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Converts {@link ExternalUserDefinition} model to {@link User} value object.
 *
 * @author Peter Smith
 */
@Component
public class ExternalUserDefinitionToUserConverter implements Converter<ExternalUserDefinition<?>, User> {

    private static final String EXTERNAL_ID_TEMPLATE = "provider=%s|ext_uid=%s";

    private final AuthenticationConfig authenticationConfig;
    private final RoleDAO roleDAO;

    @Autowired
    public ExternalUserDefinitionToUserConverter(AuthenticationConfig authenticationConfig, RoleDAO roleDAO) {
        this.authenticationConfig = authenticationConfig;
        this.roleDAO = roleDAO;
    }

    @Override
    public User convert(ExternalUserDefinition<?> externalUserDefinition) {

        return User.builder()
                .username(externalUserDefinition.getUsername())
                .email(externalUserDefinition.getEmail())
                .enabled(authenticationConfig.isUserEnabledByDefault())
                .created(new Date())
                .defaultLocale(authenticationConfig.getDefaultLocale())
                .role(roleDAO.findExternalDefault().orElseThrow(MissingDefaultRoleException::external))
                .accountType(externalUserDefinition.getAccountType())
                .externalID(formatExternalID(externalUserDefinition))
                .build();
    }

    private String formatExternalID(ExternalUserDefinition<?> externalUserDefinition) {
        return String.format(EXTERNAL_ID_TEMPLATE, externalUserDefinition.getAccountType().name().toLowerCase(), externalUserDefinition.getUserID());
    }
}
