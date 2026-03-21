package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.request.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.exception.MissingDefaultRoleException;
import hu.psprog.leaflet.lags.core.persistence.dao.RoleDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Converts {@link SignUpRequestModel} model to {@link User} value object.
 *
 * @author Peter Smith
 */
@Component
public class SignUpRequestModelToUserConverter implements Converter<SignUpRequestModel, User> {

    private final AuthenticationConfig authenticationConfig;
    private final PasswordEncoder passwordEncoder;
    private final RoleDAO roleDAO;

    @Autowired
    public SignUpRequestModelToUserConverter(AuthenticationConfig authenticationConfig, PasswordEncoder passwordEncoder, RoleDAO roleDAO) {
        this.authenticationConfig = authenticationConfig;
        this.passwordEncoder = passwordEncoder;
        this.roleDAO = roleDAO;
    }

    @Override
    public User convert(SignUpRequestModel signUpRequestModel) {

        return User.builder()
                .username(signUpRequestModel.getUsername())
                .email(signUpRequestModel.getEmail())
                .password(passwordEncoder.encode(signUpRequestModel.getPassword()))
                .enabled(authenticationConfig.isUserEnabledByDefault())
                .created(new Date())
                .defaultLocale(authenticationConfig.getDefaultLocale())
                .role(roleDAO.findLocalDefault().orElseThrow(MissingDefaultRoleException::internal))
                .accountType(AccountType.LOCAL)
                .build();
    }
}
