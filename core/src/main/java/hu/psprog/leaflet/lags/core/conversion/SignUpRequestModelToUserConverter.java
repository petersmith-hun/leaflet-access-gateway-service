package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.config.AuthenticationConfig;
import hu.psprog.leaflet.lags.core.domain.AccountType;
import hu.psprog.leaflet.lags.core.domain.Role;
import hu.psprog.leaflet.lags.core.domain.SignUpRequestModel;
import hu.psprog.leaflet.lags.core.domain.User;
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

    @Autowired
    public SignUpRequestModelToUserConverter(AuthenticationConfig authenticationConfig, PasswordEncoder passwordEncoder) {
        this.authenticationConfig = authenticationConfig;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User convert(SignUpRequestModel signUpRequestModel) {

        return User.builder()
                .username(signUpRequestModel.getUsername())
                .email(signUpRequestModel.getEmail())
                .password(passwordEncoder.encode(signUpRequestModel.getPassword()))
                .enabled(authenticationConfig.isUserEnabledByDefault())
                .created(new Date())
                .defaultLocale(authenticationConfig.getDefaultLocale().toString())
                .role(Role.USER)
                .accountType(AccountType.LOCAL)
                .build();
    }
}
