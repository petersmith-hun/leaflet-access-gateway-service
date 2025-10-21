package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Unit tests for {@link OAuthApplicationAutoImporter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthApplicationAutoImporterTest {

    @Mock
    private OAuthApplicationDAO oauthApplicationDAO;

    @Mock
    private PermissionImporter permissionImporter;

    @Mock
    private ClientImporter clientImporter;

    @InjectMocks
    private OAuthApplicationAutoImporter oAuthApplicationAutoImporter;

    @Test
    public void shouldInitExecuteImport() {

        // given
        given(oauthApplicationDAO.count()).willReturn(0L);

        // when
        oAuthApplicationAutoImporter.init();

        // then
        var importersInOrder = inOrder(permissionImporter, clientImporter);
        importersInOrder.verify(permissionImporter).importPermissions();
        importersInOrder.verify(clientImporter).importClients();
    }

    @Test
    public void shouldInitSkipImport() {

        // given
        given(oauthApplicationDAO.count()).willReturn(10L);

        // when
        oAuthApplicationAutoImporter.init();

        // then
        verifyNoInteractions(permissionImporter, clientImporter);
    }
}
