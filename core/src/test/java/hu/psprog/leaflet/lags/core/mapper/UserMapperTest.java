package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.entity.LegacyRole;
import hu.psprog.leaflet.lags.core.domain.entity.SupportedLocale;
import hu.psprog.leaflet.lags.core.domain.entity.User;
import hu.psprog.leaflet.lags.core.domain.request.UserRequest;
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

    @Mock
    private SecretGenerator secretGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserMapper userMapper;

    @Test
    public void shouldMapUserEntityToResponse() {

        // given
        var user = User.builder()
                .id(1L)
                .username("username")
                .email("user1@dev.local")
                .role(LegacyRole.EXTERNAL_USER)
                .defaultLocale(SupportedLocale.EN)
                .accountType(AccountType.GITHUB)
                .externalID("githubID=1")
                .enabled(true)
                .created(Date.from(Instant.parse("2026-02-20T18:56:30.000+01:00")))
                .lastModified(Date.from(Instant.parse("2026-02-21T15:00:10.000+01:00")))
                .lastLogin(Date.from(Instant.parse("2026-02-21T13:00:10.000+01:00")))
                .build();

        var expectedResponse = UserDetailsResponse.builder()
                .id(1L)
                .username("username")
                .email("user1@dev.local")
                .role(LegacyRole.EXTERNAL_USER)
                .locale(SupportedLocale.EN)
                .accountType(AccountType.GITHUB)
                .externalID("githubID=1")
                .enabled(true)
                .created(ZonedDateTime.of(2026, 2, 20, 18, 56, 30, 0, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.of(2026, 2, 21, 15, 0, 10, 0, ZoneId.systemDefault()))
                .lastLogin(ZonedDateTime.of(2026, 2, 21, 13, 0, 10, 0, ZoneId.systemDefault()))
                .build();

        // when
        var result = userMapper.map(user);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldMapRequestToEntity() {

        // given
        var request = UserRequest.builder()
                .username("username")
                .email("user2@dev.local")
                .defaultLocale(SupportedLocale.HU)
                .role(LegacyRole.ADMIN)
                .build();

        var expectedUser = User.builder()
                .username("username")
                .email("user2@dev.local")
                .defaultLocale(SupportedLocale.HU)
                .role(LegacyRole.ADMIN)
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
}
