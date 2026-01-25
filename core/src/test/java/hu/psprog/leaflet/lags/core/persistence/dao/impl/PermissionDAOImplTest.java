package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PermissionDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionDAOImplTest {

    @Mock
    private Pageable pageable;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionDAOImpl permissionDAO;

    @Test
    public void shouldFindAllByNames() {

        // given
        var permissions = List.of("permission-1", "permission-2", "permission-3");

        // when
        permissionDAO.findAllByNames(permissions);

        // then
        verify(permissionRepository).findAllByNameIn(permissions);
    }

    @Test
    public void shouldFindAll() {

        // when
        permissionDAO.findAll(pageable);

        // then
        verify(permissionRepository).findAll(pageable);
    }

    @Test
    public void shouldFindByID() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionDAO.findByID(id);

        // then
        verify(permissionRepository).findById(id);
    }

    @Test
    public void shouldSave() {

        // given
        var permission = Permission.builder()
                .name("permission-1")
                .build();

        // when
        permissionDAO.save(permission);

        // then
        verify(permissionRepository).save(permission);
    }

    @Test
    public void shouldSaveAll() {

        // given
        var permissions = List.of(Permission.builder().name("permission-1").build());

        // when
        permissionDAO.saveAll(permissions);

        // then
        verify(permissionRepository).saveAll(permissions);
    }

    @Test
    public void shouldDelete() {

        // given
        var id = UUID.randomUUID();

        // when
        permissionDAO.delete(id);

        // then
        verify(permissionRepository).deleteById(id);
    }
}
