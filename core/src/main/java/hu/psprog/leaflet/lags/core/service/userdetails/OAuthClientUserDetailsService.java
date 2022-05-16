package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link UserDetailsService} implementation to look up registered OAuth2 clients.
 * Clients are retrieved via the an {@link OAuthClientRegistry} implementation.
 *
 * @author Peter Smith
 */
@Service
public class OAuthClientUserDetailsService implements UserDetailsService {

    private static final String OAUTH_CLIENT_NOT_FOUND_MESSAGE_PATTERN = "OAuth client identified by client ID [%s] not found";

    private final OAuthClientRegistry oAuthClientRegistry;

    @Autowired
    public OAuthClientUserDetailsService(OAuthClientRegistry oAuthClientRegistry) {
        this.oAuthClientRegistry = oAuthClientRegistry;
    }

    /**
     * Retrieves a registered OAuth2 client by its client ID.
     *
     * Mapping of the registered client's information happens as defined below:
     *  - client ID -> username
     *  - client secret -> password
     *  - audience -> roles
     *  - registered scopes -> authorities
     *
     * @param clientID client ID of the OAuth2 client
     * @return registered OAuth2 client converted to {@link UserDetails} object
     * @throws UsernameNotFoundException if a registered OAuth2 client by the specified client ID does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String clientID) throws UsernameNotFoundException {

        return oAuthClientRegistry.getClientByClientID(clientID)
                .map(oAuthClient -> User.builder()
                        .username(oAuthClient.getClientId())
                        .password(oAuthClient.getClientSecret())
                        .roles(oAuthClient.getAudience())
                        .authorities(createAuthorityList(oAuthClient))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(String.format(OAUTH_CLIENT_NOT_FOUND_MESSAGE_PATTERN, clientID)));
    }

    private List<GrantedAuthority> createAuthorityList(OAuthClient oAuthClient) {
        return AuthorityUtils.createAuthorityList(oAuthClient.getRegisteredScopes().toArray(String[]::new));
    }
}
