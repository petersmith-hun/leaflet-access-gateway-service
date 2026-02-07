package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.domain.request.RoleRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import hu.psprog.leaflet.lags.core.domain.response.RoleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link RoleMapper}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RoleMapperTest {

    private static final UUID ROLE_ID = UUID.randomUUID();
    private static final String ROLE_NAME = "role";
    private static final String ROLE_DESCRIPTION = "description";
    private static final boolean LOCAL_DEFAULT = true;
    private static final boolean EXTERNAL_DEFAULT = true;
    private static final boolean ENABLED = true;
    private static final Instant BASELINE_INSTANT_CREATED = Instant.ofEpochMilli(System.currentTimeMillis());
    private static final Instant BASELINE_INSTANT_UPDATED = BASELINE_INSTANT_CREATED.plusSeconds(60);
    private static final String PERMISSION_NAME = "permission";

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private RoleMapper roleMapper;

    @Test
    public void shouldMapRoleToResponse() {

        // given
        var role = Role.builder()
                .id(ROLE_ID)
                .name(ROLE_NAME)
                .description(ROLE_DESCRIPTION)
                .localDefault(LOCAL_DEFAULT)
                .externalDefault(EXTERNAL_DEFAULT)
                .enabled(ENABLED)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .permissions(List.of(Permission.builder()
                        .name(PERMISSION_NAME)
                        .build()))
                .build();

        var expectedResponse = RoleResponse.builder()
                .id(ROLE_ID)
                .name(ROLE_NAME)
                .description(ROLE_DESCRIPTION)
                .localDefault(LOCAL_DEFAULT)
                .externalDefault(EXTERNAL_DEFAULT)
                .enabled(ENABLED)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .permissions(List.of(PermissionResponse.builder()
                        .name(PERMISSION_NAME)
                        .build()))
                .build();

        given(permissionMapper.map(role.getPermissions().getFirst()))
                .willReturn(expectedResponse.permissions().getFirst());

        // when
        var result = roleMapper.map(role);

        // then
        assertThat(result, equalTo(expectedResponse));
    }

    @Test
    public void shouldMapRequestToRole() {

        // given
        var request = RoleRequest.builder()
                .name(ROLE_NAME)
                .description(ROLE_DESCRIPTION)
                .build();

        var expected = Role.builder()
                .name(ROLE_NAME)
                .description(ROLE_DESCRIPTION)
                .enabled(true)
                .build();

        // when
        var result = roleMapper.map(request);

        // then
        assertThat(result, equalTo(expected));
    }
}
