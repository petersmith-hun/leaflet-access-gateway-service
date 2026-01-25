package hu.psprog.leaflet.lags.core.mapper;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit tests for {@link PermissionMapper}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionMapperTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String PERMISSION_NAME = "read:any";
    private static final String PERMISSION_DESCRIPTION = "Permission description";
    private static final boolean ENABLED = true;
    private static final Instant BASELINE_INSTANT_CREATED = Instant.ofEpochMilli(System.currentTimeMillis());
    private static final Instant BASELINE_INSTANT_UPDATED = BASELINE_INSTANT_CREATED.plusSeconds(60);

    @InjectMocks
    private PermissionMapper permissionMapper;

    @Test
    public void shouldMapPermissionEntityToResponse() {

        // given
        var source = Permission.builder()
                .id(ID)
                .name(PERMISSION_NAME)
                .description(PERMISSION_DESCRIPTION)
                .enabled(ENABLED)
                .createdAt(Date.from(BASELINE_INSTANT_CREATED))
                .updatedAt(Date.from(BASELINE_INSTANT_UPDATED))
                .build();

        var expected = PermissionResponse.builder()
                .id(ID)
                .name(PERMISSION_NAME)
                .description(PERMISSION_DESCRIPTION)
                .enabled(ENABLED)
                .created(ZonedDateTime.ofInstant(BASELINE_INSTANT_CREATED, ZoneId.systemDefault()))
                .lastModified(ZonedDateTime.ofInstant(BASELINE_INSTANT_UPDATED, ZoneId.systemDefault()))
                .build();

        // when
        var result = permissionMapper.map(source);

        // then
        assertThat(result, equalTo(expected));
    }

    @Test
    public void shouldMapPermissionRequestToEntity() {

        // given
        var request = PermissionRequest.builder()
                .name(PERMISSION_NAME)
                .description(PERMISSION_DESCRIPTION)
                .build();

        var expected = Permission.builder()
                .name(PERMISSION_NAME)
                .description(PERMISSION_DESCRIPTION)
                .build();

        // when
        var result = permissionMapper.map(request);

        // then
        assertThat(result, equalTo(expected));
    }
}
