package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
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
 * Unit tests for {@link AllLocalUserUserDetailsService}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AllLocalUserUserDetailsServiceTest {

    private static final String LOCAL_USER_EMAIL = "admin@dev.local";
    private static final String EXTERNAL_USER_EMAIL = "externaluser1@dev.local";
    private static final User LOCAL_USER = prepareUser(AccountType.LOCAL);
    private static final User EXTERNAL_USER = prepareUser(AccountType.GITHUB);
    private static final List<GrantedAuthority> AUTHORITIES = AuthorityUtils.createAuthorityList("read:all", "write:all");
    private static final ExtendedUser EXPECTED_EXTENDED_LOCAL_USER = prepareExpectedExtendedUser(LOCAL_USER);
    private static final ExtendedUser EXPECTED_EXTENDED_EXTERNAL_USER = prepareExpectedExtendedUser(EXTERNAL_USER);

    @Mock
    private UserDAO userDAO;

    @Mock
    private RoleToAuthorityMappingRegistry roleToAuthorityMappingRegistry;

    @InjectMocks
    private AllLocalUserUserDetailsService allLocalUserUserDetailsService;

    @Test
    public void shouldLoadUserByUsernameSuccessfullyLoadLocalUser() {

        // given
        given(userDAO.findByEmail(LOCAL_USER_EMAIL)).willReturn(Optional.of(LOCAL_USER));
        given(roleToAuthorityMappingRegistry.getAuthoritiesForRole(LegacyRole.ADMIN)).willReturn(AUTHORITIES);

        // when
        UserDetails result = allLocalUserUserDetailsService.loadUserByUsername(LOCAL_USER_EMAIL);

        // then
        assertThat(result instanceof ExtendedUser, is(true));
        assertThat(result, equalTo(EXPECTED_EXTENDED_LOCAL_USER));
    }

    @Test
    public void shouldLoadUserByUsernameSuccessfullyLoadExternalUser() {

        // given
        given(userDAO.findByEmail(EXTERNAL_USER_EMAIL)).willReturn(Optional.of(EXTERNAL_USER));
        given(roleToAuthorityMappingRegistry.getAuthoritiesForRole(LegacyRole.USER)).willReturn(AUTHORITIES);

        // when
        UserDetails result = allLocalUserUserDetailsService.loadUserByUsername(EXTERNAL_USER_EMAIL);

        // then
        assertThat(result instanceof ExtendedUser, is(true));
        assertThat(result, equalTo(EXPECTED_EXTENDED_EXTERNAL_USER));
    }

    @Test
    public void shouldLoadUserByUsernameThrowExceptionOnMissingUser() {

        // given
        given(userDAO.findByEmail(LOCAL_USER_EMAIL)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(UsernameNotFoundException.class, () -> allLocalUserUserDetailsService.loadUserByUsername(LOCAL_USER_EMAIL));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("User identified by email address [admin@dev.local] not found"));
    }

    private static User prepareUser(AccountType accountType) {

        User user = new User();
        user.setPassword("password1" + accountType);
        user.setUsername("user1" + accountType);
        user.setEnabled(true);
        user.setAccountType(accountType);
        if (accountType == AccountType.LOCAL) {
            user.setEmail(LOCAL_USER_EMAIL);
            user.setId(1234L);
            user.setRole(LegacyRole.ADMIN);
        } else {
            user.setEmail(EXTERNAL_USER_EMAIL);
            user.setId(5678L);
            user.setRole(LegacyRole.USER);
        }

        return user;
    }

    private static ExtendedUser prepareExpectedExtendedUser(User sourceUser) {

        return ExtendedUser.builder()
                .username(sourceUser.getEmail())
                .password(sourceUser.getPassword())
                .name(sourceUser.getUsername())
                .id(sourceUser.getId())
                .enabled(sourceUser.isEnabled())
                .role(sourceUser.getRole().toString())
                .authorities(AUTHORITIES)
                .build();
    }
}
