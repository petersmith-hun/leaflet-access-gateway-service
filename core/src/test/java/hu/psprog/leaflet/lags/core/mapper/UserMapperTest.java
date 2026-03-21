package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
import hu.psprog.leaflet.lags.core.domain.response.ProfileModel;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import hu.psprog.leaflet.lags.core.domain.response.UserDetailsResponse;
import hu.psprog.leaflet.lags.core.service.util.SecretGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link UserMapper}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private static final Instant BASELINE_INSTANT_CREATED = Instant.ofEpochMilli(System.currentTimeMillis());
    private static final Instant BASELINE_INSTANT_UPDATED = BASELINE_INSTANT_CREATED.plusSeconds(60);
    private static final Instant BASELINE_INSTANT_LAST_LOGIN = BASELINE_INSTANT_CREATED.minusSeconds(60);

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    public void shouldMapUserEntityToResponse() {

        // given
        var role = Role.builder()
                .name("role")
                .build();

        var expectedRole = RoleResponse.builder()
                .name("role")
                .build();

        var user = User.builder()
                .id(1L)
                .username("username")
                .email("user1@dev.local")
                .role(role)
                .defaultLocale(SupportedLocale.EN)
                .accountType(AccountType.GITHUB)
                .externalID("githubID=1")
                .enabled(true)
                .created(Date.from(BASELINE_INSTANT_CREATED))
                .lastModified(Date.from(BASELINE_INSTANT_UPDATED))
                .lastLogin(Date.from(BASELINE_INSTANT_LAST_LOGIN))
                .build();

        var expectedResponse = UserDetailsResponse.builder()
                .id(1L)
                .username("username")
                .email("user1@dev.local")
                .role(expectedRole)
                .locale(SupportedLocale.EN)
                .accountType(AccountType.GITHUB)
                .externalID("githubID=1")
                .enabled(true)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .lastLogin(ZonedDateTime.ofInstant(BASELINE_INSTANT_LAST_LOGIN, ZoneId.systemDefault()))
                .build();

        given(roleMapper.map(role)).willReturn(expectedRole);

        // when
        var result = userMapper.map(user);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldMapRequestToEntity() {

        // given
        var roleID = UUID.randomUUID();

        var request = UserRequest.builder()
                .username("username")
                .email("user2@dev.local")
                .defaultLocale(SupportedLocale.HU)
                .roleID(roleID)
                .build();

        var expectedUser = User.builder()
                .username("username")
                .email("user2@dev.local")
                .defaultLocale(SupportedLocale.HU)
                .role(Role.builder()
                        .id(roleID)
                        .build())
                .password("encrypted-password")
                .enabled(true)
                .accountType(AccountType.LOCAL)
                .build();

        given(secretGenerator.generateSecret()).willReturn("raw-password");
        given(passwordEncoder.encode("raw-password")).willReturn("encrypted-password");

        // when
        var result = userMapper.map(request);

        // then
        assertThat(result, equalTo(expectedUser));
    }

    @Test
    public void shouldMapEntityToProfileModel() {

        // given
        var user = User.builder()
                .username("username")
                .email("user2@dev.local")
                .defaultLocale(SupportedLocale.HU)
                .build();

        var expectedProfileModel = ProfileModel.builder()
                .username("username")
                .email("user2@dev.local")
                .locale(SupportedLocale.HU.name())
                .build();

        // when
        var result = userMapper.mapToProfile(user);

        // then
        assertThat(result, equalTo(expectedProfileModel));
    }
}
