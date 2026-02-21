package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.util.SecretGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * User data mapping operations.
 *
 * @author Peter Smith
 */
@Component
public class UserMapper extends AbstractCommonMapper {

    private static final boolean DEFAULT_ENABLED = true;
    private static final AccountType DEFAULT_ACCOUNT_TYPE = AccountType.LOCAL;

    private final SecretGenerator secretGenerator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(SecretGenerator secretGenerator, PasswordEncoder passwordEncoder) {
        this.secretGenerator = secretGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Maps the given {@link User} entity to {@link UserDetailsResponse} (API response model).
     *
     * @param user source {@link User} entity object
     * @return mapped {@link UserDetailsResponse} object
     */
    public UserDetailsResponse map(User user) {

        return UserDetailsResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .locale(user.getDefaultLocale())
                .accountType(user.getAccountType())
                .externalID(user.getExternalID())
                .enabled(user.isEnabled())
                .created(convertDate(user.getCreated()))
                .lastModified(convertDate(user.getLastModified()))
                .lastLogin(convertDate(user.getLastLogin()))
                .build();
    }

    /**
     * Maps the given {@link UserRequest} API request model to {@link User} entity.
     *
     * @param request source {@link UserRequest} model object
     * @return mapped {@link User} entity
     */
    public User map(UserRequest request) {

        return User.builder()
                .username(request.username())
                .email(request.email())
                .defaultLocale(request.defaultLocale())
                .role(request.role())
                .password(passwordEncoder.encode(secretGenerator.generateSecret()))
                .enabled(DEFAULT_ENABLED)
                .accountType(DEFAULT_ACCOUNT_TYPE)
                .build();
    }
}
