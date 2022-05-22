package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.persistence.dao.UserDAO;
import hu.psprog.leaflet.lags.core.service.registry.RoleToAuthorityMappingRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link LocalUserUserDetailsService}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class LocalUserUserDetailsServiceTest {

    private static final String EMAIL = "admin@dev.local";
    private static final User USER = prepareUser();
    private static final List<GrantedAuthority> AUTHORITIES = AuthorityUtils.createAuthorityList("read:all", "write:all");
    private static final ExtendedUser EXPECTED_EXTENDED_USER = prepareExpectedExtendedUser();

    @Mock
    private UserDAO userDAO;

    @Mock
    private RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry;

    @InjectMocks
    private LocalUserUserDetailsService localUserUserDetailsService;

    @Test
    public void shouldLoadUserByUsernameSuccessfullyLoadUser() {

        // given
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.of(USER));
        given(roleToAuthorityMappingRegistry.getAuthoritiesForRole(Role.USER)).willReturn(AUTHORITIES);

        // when
        UserDetails result = localUserUserDetailsService.loadUserByUsername(EMAIL);

        // then
        assertThat(result instanceof ExtendedUser, is(true));
        assertThat(result, equalTo(EXPECTED_EXTENDED_USER));
    }

    @Test
    public void shouldLoadUserByUsernameThrowExceptionOnMissingUser() {

        // given
        given(userDAO.findByEmail(EMAIL)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(UsernameNotFoundException.class, () -> localUserUserDetailsService.loadUserByUsername(EMAIL));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("User identified by email address [admin@dev.local] not found"));
    }

    private static User prepareUser() {

        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword("password1");
        user.setUsername("user1");
        user.setId(1234L);
        user.setEnabled(true);
        user.setRole(Role.USER);

        return user;
    }

    private static ExtendedUser prepareExpectedExtendedUser() {

        return ExtendedUser.builder()
                .username(EMAIL)
                .password(USER.getPassword())
                .name(USER.getUsername())
                .id(USER.getId())
                .enabled(USER.isEnabled())
                .role(USER.getRole().toString())
                .authorities(AUTHORITIES)
                .build();
    }
}
