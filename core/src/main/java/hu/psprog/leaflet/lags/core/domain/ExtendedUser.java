package hu.psprog.leaflet.lags.core.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Spring Security compatible {@link UserDetails} implementation containing extra user information.
 *
 * @author Peter Smith
 */
@Data
@Builder
public class ExtendedUser implements UserDetails {

    private final String username;
    private final String password;
    private final String name;
    private final Long id;
    private final boolean enabled;
    private final String role;
    private final Collection<GrantedAuthority> authorities;

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }
}
