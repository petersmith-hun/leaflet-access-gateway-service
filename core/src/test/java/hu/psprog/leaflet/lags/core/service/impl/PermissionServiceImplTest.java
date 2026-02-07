package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.domain.request.PermissionRequest;
import hu.psprog.leaflet.lags.core.domain.response.PermissionResponse;
import hu.psprog.leaflet.lags.core.exception.ConflictingResourceException;
import hu.psprog.leaflet.lags.core.exception.ResourceNotFoundException;
import hu.psprog.leaflet.lags.core.mapper.PermissionMapper;
import hu.psprog.leaflet.lags.core.persistence.dao.PermissionDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PermissionServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionDAO permissionDAO;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    public void shouldGetPermissions() {

        // given
        var page = 1;
        var permissionID = UUID.randomUUID();
        var permissions = List.of(Permission.builder().id(permissionID).build());

        var expectedPageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());
        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findAll(expectedPageRequest)).willReturn(new PageImpl<>(permissions));
        given(permissionMapper.map(permissions.getFirst())).willReturn(expectedPermission);

        // when
        var result = permissionService.getPermissions(page);

        // then
        assertThat(result.getContent(), equalTo(List.of(expectedPermission)));
    }

    @Test
    public void shouldGetPermissionsUnpaged() {

        // given
        var page = 0;
        var permissionID = UUID.randomUUID();
        var permissions = List.of(Permission.builder().id(permissionID).build());

        var expectedPageRequest = Pageable.unpaged(Sort.by("name").ascending());
        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findAll(expectedPageRequest)).willReturn(new PageImpl<>(permissions));
        given(permissionMapper.map(permissions.getFirst())).willReturn(expectedPermission);

        // when
        var result = permissionService.getPermissions(page);

        // then
        assertThat(result.getContent(), equalTo(List.of(expectedPermission)));
    }

    @Test
    public void shouldGetPermission() {

        // given
        var permissionID = UUID.randomUUID();
        var permission = Permission.builder()
                .id(permissionID)
                .build();

        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findByID(permissionID)).willReturn(Optional.of(permission));
        given(permissionMapper.map(permission)).willReturn(expectedPermission);

        // when
        var result = permissionService.getPermission(permissionID);

        // then
        assertThat(result, equalTo(expectedPermission));
    }

    @Test
    public void shouldGetPermissionThrowExceptionOnMissingPermission() {

        // given
        var permissionID = UUID.randomUUID();

        given(permissionDAO.findByID(permissionID)).willReturn(Optional.empty());

        // when
        assertThrows(ResourceNotFoundException.class,
                () -> permissionService.getPermission(permissionID));

        // then
        // exception expected
    }

    @Test
    public void shouldCreatePermission() {

        // given
        var permissionID = UUID.randomUUID();
        var permissionName = "read:any";
        var request = PermissionRequest.builder().build();
        var mappedEntity = Permission.builder()
                .name(permissionName)
                .build();
        var savedEntity = Permission.builder()
                .id(permissionID)
                .name(permissionName)
                .build();

        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionMapper.map(request)).willReturn(mappedEntity);
        given(permissionDAO.save(mappedEntity)).willReturn(savedEntity);
        given(permissionMapper.map(savedEntity)).willReturn(expectedPermission);

        // when
        var result = permissionService.createPermission(request);

        // then
        assertThat(result, equalTo(expectedPermission));
    }

    @Test
    public void shouldCreatePermissionThrowExceptionOnDuplicatePermission() {

        // given
        var permissionName = "read:any";
        var request = PermissionRequest.builder().build();
        var mappedEntity = Permission.builder()
                .name(permissionName)
                .build();

        given(permissionMapper.map(request)).willReturn(mappedEntity);
        given(permissionDAO.save(mappedEntity)).willThrow(DataIntegrityViolationException.class);

        // when
        assertThrows(ConflictingResourceException.class,
                () -> permissionService.createPermission(request));

        // then
        // exception expected
    }

    @Test
    public void shouldEditPermission() {

        // given
        var permissionID = UUID.randomUUID();
        var permissionName = "read:any";
        var permissionDescription = "description";
        var request = PermissionRequest.builder()
                .name(permissionName)
                .description(permissionDescription)
                .build();
        var currentEntity = Permission.builder()
                .id(permissionID)
                .build();

        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findByID(permissionID)).willReturn(Optional.of(currentEntity));
        given(permissionDAO.save(currentEntity)).willReturn(currentEntity);
        given(permissionMapper.map(currentEntity)).willReturn(expectedPermission);

        // when
        var result = permissionService.editPermission(permissionID, request);

        // then
        assertThat(result, equalTo(expectedPermission));
        assertThat(currentEntity.getName(), equalTo(permissionName));
        assertThat(currentEntity.getDescription(), equalTo(permissionDescription));
    }

    @Test
    public void shouldUpdatePermissionStatusToEnabled() {

        // given
        var permissionID = UUID.randomUUID();
        var permissionName = "read:any";
        var currentEntity = Permission.builder()
                .id(permissionID)
                .enabled(false)
                .name(permissionName)
                .build();

        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findByID(permissionID)).willReturn(Optional.of(currentEntity));
        given(permissionMapper.map(currentEntity)).willReturn(expectedPermission);

        // when
        var result = permissionService.updatePermissionStatus(permissionID, true);

        // then
        assertThat(result, equalTo(expectedPermission));
        assertThat(currentEntity.isEnabled(), equalTo(true));
    }

    @Test
    public void shouldUpdatePermissionStatusToDisabled() {

        // given
        var permissionID = UUID.randomUUID();
        var permissionName = "read:any";
        var currentEntity = Permission.builder()
                .id(permissionID)
                .enabled(true)
                .name(permissionName)
                .build();

        var expectedPermission = PermissionResponse.builder()
                .id(permissionID)
                .build();

        given(permissionDAO.findByID(permissionID)).willReturn(Optional.of(currentEntity));
        given(permissionMapper.map(currentEntity)).willReturn(expectedPermission);

        // when
        var result = permissionService.updatePermissionStatus(permissionID, false);

        // then
        assertThat(result, equalTo(expectedPermission));
        assertThat(currentEntity.isEnabled(), equalTo(false));
    }

    @Test
    public void shouldDeletePermission() {

        // given
        var permissionID = UUID.randomUUID();

        // when
        permissionService.deletePermission(permissionID);

        // then
        verify(permissionDAO).delete(permissionID);
    }

    @Test
    public void shouldDeletePermissionThrowExceptionOnDataIntegrityViolation() {

        // given
        var permissionID = UUID.randomUUID();

        doThrow(DataIntegrityViolationException.class).when(permissionDAO).delete(permissionID);

        // when
        assertThrows(ConflictingResourceException.class,
                () -> permissionService.deletePermission(permissionID));

        // then
        // exception expected
    }
}
