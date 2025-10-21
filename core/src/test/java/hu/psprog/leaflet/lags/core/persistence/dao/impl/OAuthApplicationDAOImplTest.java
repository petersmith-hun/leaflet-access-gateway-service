package hu.psprog.leaflet.lags.core.persistence.dao.impl;

import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.persistence.repository.OAuthApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link OAuthApplicationDAOImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationDAOImplTest {

    @Mock
    private OAuthApplicationRepository oAuthApplicationRepository;

    @InjectMocks
    private OAuthApplicationDAOImpl oAuthApplicationDAO;

    @Test
    public void shouldSave() {

        // given
        var entity = OAuthApplication.builder().build();

        // when
        oAuthApplicationDAO.save(entity);

        // then
        verify(oAuthApplicationRepository).saveAndFlush(entity);
    }

    @Test
    public void shouldFindByName() {

        // given
        var name = "client-1";

        // when
        oAuthApplicationDAO.findByName(name);

        // then
        verify(oAuthApplicationRepository).findByName(name);
    }

    @Test
    public void shouldFindByClientID() {

        // given
        var clientID = "client-id-1";

        // when
        oAuthApplicationDAO.findByClientID(clientID);

        // then
        verify(oAuthApplicationRepository).findByClientId(clientID);
    }

    @Test
    public void shouldFindByAudience() {

        // given
        var audience = "audience-1";

        // when
        oAuthApplicationDAO.findByAudience(audience);

        // then
        verify(oAuthApplicationRepository).findByAudience(audience);
    }

    @Test
    public void shouldCount() {

        // when
        oAuthApplicationDAO.count();

        // then
        verify(oAuthApplicationRepository).count();
    }
}
