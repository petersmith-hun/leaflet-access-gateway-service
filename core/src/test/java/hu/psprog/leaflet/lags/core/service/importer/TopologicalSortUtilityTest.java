package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link TopologicalSortUtility}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class TopologicalSortUtilityTest {

    @Mock
    private OAuthConfigurationProperties oAuthConfigurationProperties;

    @InjectMocks
    private TopologicalSortUtility topologicalSortUtility;

    @Test
    public void shouldTopologicalSortClients() {

        // given
        var clients = List.of(
                client("app1", "app2", "app3"),
                client("app2"),
                client("app3", "app4"),
                client("app4", "app5"),
                client("app5")
        );

        var expectedResult = List.of(
                "app5",
                "app2",
                "app4",
                "app3",
                "app1"
        );

        given(oAuthConfigurationProperties.getClients()).willReturn(clients);

        // when
        var result = topologicalSortUtility.topologicalSortClients();

        // then
        assertThat(result, equalTo(expectedResult));
    }

    private OAuthClient client(String name, String... allowedClients) {

        return OAuthClient.builder()
                .clientName(name)
                .allowedClients(Stream.of(allowedClients)
                        .map(allowedClientName -> OAuthClientAllowRelation.builder()
                                .name(allowedClientName)
                                .build())
                        .toList())
                .build();
    }
}
