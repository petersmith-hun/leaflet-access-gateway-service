package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.Role;
import hu.psprog.leaflet.lags.core.persistence.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RoleDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class RoleDAOImplTest {

    private final static UUID ID = UUID.randomUUID();
    private final static Role ROLE = new Role();

    @Mock
    private Pageable pageable;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleDAOImpl roleDAO;

    @Test
    public void shouldFindAll() {

        // when
        roleDAO.findAll(pageable);

        // then
        verify(roleRepository).findAll(pageable);
    }

    @Test
    public void shouldFindByID() {

        // when
        roleDAO.findByID(ID);

        // then
        verify(roleRepository).findById(ID);
    }

    @Test
    public void shouldSave() {

        // when
        roleDAO.save(ROLE);

        // then
        verify(roleRepository).save(ROLE);
    }

    @Test
    public void shouldDelete() {

        // when
        roleDAO.delete(ID);

        // then
        verify(roleRepository).deleteById(ID);
    }

    @Test
    public void shouldRemoveCurrentLocalDefaultFlag() {

        // when
        roleDAO.removeCurrentLocalDefaultFlag();

        // then
        verify(roleRepository).removeCurrentLocalDefaultFlag();
    }

    @Test
    public void shouldRemoveCurrentExternalDefaultFlag() {

        // when
        roleDAO.removeCurrentExternalDefaultFlag();

        // then
        verify(roleRepository).removeCurrentExternalDefaultFlag();
    }
}
