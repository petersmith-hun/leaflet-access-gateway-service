package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.exception.OAuthApplicationImportException;
import hu.psprog.leaflet.lags.core.persistence.dao.OAuthApplicationDAO;
import hu.psprog.leaflet.lags.core.service.factory.OAuthApplicationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

/**
 * Unit tests for {@link ClientImporter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ClientImporterTest {

    @Mock
    private OAuthConfigurationProperties oAuthConfigurationProperties;

    @Mock
    private OAuthApplicationFactory oauthApplicationFactory;

    @Mock
    private OAuthApplicationDAO oauthApplicationDAO;

    @Mock
    private TopologicalSortUtility topologicalSortUtility;

    @InjectMocks
    private ClientImporter clientImporter;

    @Test
    public void shouldImportClients() {

        // given
        var client1 = client("app1");
        var client2 = client("app2");
        var client3 = client("app3");

        var application1 = application("app1");
        var application2 = application("app2");
        var application3 = application("app3");

        given(topologicalSortUtility.topologicalSortClients()).willReturn(List.of("app1", "app2", "app3"));
        given(oAuthConfigurationProperties.getClients()).willReturn(List.of(client1, client2, client3));
        given(oauthApplicationFactory.create(client1)).willReturn(application1);
        given(oauthApplicationFactory.create(client2)).willReturn(application2);
        given(oauthApplicationFactory.create(client3)).willReturn(application3);

        // when
        clientImporter.importClients();

        // then
        var daoCallsInOrder = inOrder(oauthApplicationDAO);
        daoCallsInOrder.verify(oauthApplicationDAO).save(application1);
        daoCallsInOrder.verify(oauthApplicationDAO).save(application2);
        daoCallsInOrder.verify(oauthApplicationDAO).save(application3);
    }

    @Test
    public void shouldImportClientsThrowExceptionOnMissingApplication() {

        // given
        var client1 = client("app1");
        var client3 = client("app3");

        var application1 = application("app1");

        given(topologicalSortUtility.topologicalSortClients()).willReturn(List.of("app1", "app2", "app3"));
        given(oAuthConfigurationProperties.getClients()).willReturn(List.of(client1, client3));
        given(oauthApplicationFactory.create(client1)).willReturn(application1);

        // when
        var exception = assertThrows(OAuthApplicationImportException.class, () -> clientImporter.importClients());

        // then
        // exception expected
        assertThat(exception.getMessage(), equalTo("OAuth application registration by name app2 not found"));
    }

    private OAuthClient client(String name) {

        return OAuthClient.builder()
                .clientId("id:%s".formatted(name))
                .clientName(name)
                .build();
    }

    private OAuthApplication application(String name) {

        return OAuthApplication.builder()
                .name(name)
                .build();
    }
}
