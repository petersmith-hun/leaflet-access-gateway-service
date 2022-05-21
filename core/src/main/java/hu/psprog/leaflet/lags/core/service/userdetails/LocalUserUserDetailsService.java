package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} implementation to look up local resource owner users.
 * This implementation is communicating with Leaflet's own user database.
 *
 * @author Peter Smith
 */
@Service
public class LocalUserUserDetailsService implements UserDetailsService {

    private static final String USERNAME_NOT_FOUND_MESSAGE_PATTERN = "User identified by email address [%s] not found";

    private final UserDAO userDAO;
    private final RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry;

    @Autowired
    public LocalUserUserDetailsService(UserDAO userDAO, RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry) {
        this.userDAO = userDAO;
        this.roleToAuthorityMappingRegistry = roleToAuthorityMappingRegistry;
    }

    /**
     * Retrieves an existing user by their email address.
     *
     * Mapping of the registered user's information happens as defined below:
     *  - email -> username
     *  - password -> password
     *  - username -> user
     *  - id -> id
     *  - enabled -> enabled
     *  - role -> role
     *  - scopes available for the user by their role -> authorities
     *
     * @param email email address of the user
     * @return identified user converted to {@link ExtendedUser} object
     * @throws UsernameNotFoundException if a registered user by the specified email address does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return userDAO.findByEmail(email)
                .map(user -> ExtendedUser.builder()
                        .username(email)
                        .password(user.getPassword())
                        .name(user.getUsername())
                        .id(user.getId())
                        .enabled(user.isEnabled())
                        .role(user.getRole().name())
                        .authorities(roleToAuthorityMappingRegistry.getAuthoritiesForRole(user.getRole()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USERNAME_NOT_FOUND_MESSAGE_PATTERN, email)));
    }
}
