package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import hu.psprog.leaflet.lags.core.persistence.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PermissionDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PermissionDAOImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionDAOImpl permissionDAO;

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
    public void shouldFindAllByNames() {

        // given
        var permissions = List.of("permission-1", "permission-2", "permission-3");

        // when
        permissionDAO.findAllByNames(permissions);

        // then
        verify(permissionRepository).findAllByNameIn(permissions);
    }
}
