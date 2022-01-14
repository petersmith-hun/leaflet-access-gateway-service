package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuthClientUserDetailsService}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthClientUserDetailsServiceTest {

    private static final String CLIENT_ID = "client1";
    private static final OAuthClient O_AUTH_CLIENT = prepareOAuthClient();
    private static final UserDetails EXPECTED_USER = prepareExpectedUser();

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @InjectMocks
    private OAuthClientUserDetailsService oAuthClientUserDetailsService;

    @Test
    public void shouldLoadUserByUsernameSuccessfullyLoadClient() {

        // given
        given(oAuthClientRegistry.getClientByClientID(CLIENT_ID)).willReturn(Optional.of(O_AUTH_CLIENT));

        // when
        UserDetails result = oAuthClientUserDetailsService.loadUserByUsername(CLIENT_ID);

        // then
        assertThat(result, equalTo(EXPECTED_USER));
    }

    @Test
    public void shouldLoadUserByUsernameThrowExceptionOnNonRegisteredClient() {

        // given
        given(oAuthClientRegistry.getClientByClientID(CLIENT_ID)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(UsernameNotFoundException.class, () -> oAuthClientUserDetailsService.loadUserByUsername(CLIENT_ID));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth client identified by client ID [client1] not found"));
    }

    private static OAuthClient prepareOAuthClient() {

        return new OAuthClient(
                "client-name",
                ApplicationType.SERVICE,
                "client1",
                "client-secret-1234",
                "audience1",
                Arrays.asList("read:all", "write:all"),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    private static UserDetails prepareExpectedUser() {

        return User.builder()
                .username(O_AUTH_CLIENT.getClientId())
                .password(O_AUTH_CLIENT.getClientSecret())
                .roles(O_AUTH_CLIENT.getAudience())
                .authorities(AuthorityUtils.createAuthorityList("read:all", "write:all"))
                .build();
    }
}
