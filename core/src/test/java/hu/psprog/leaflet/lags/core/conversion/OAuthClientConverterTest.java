package hu.psprog.leaflet.lags.core.conversion;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthAllowedClient;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthApplication;
import hu.psprog.leaflet.lags.core.domain.entity.OAuthCallback;
import hu.psprog.leaflet.lags.core.domain.entity.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link OAuthClientConverter}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthClientConverterTest {

    @InjectMocks
    private OAuthClientConverter oAuthClientConverter;

    @Test
    public void shouldConvertFullObject() {

        // given
        var allowedClient1 = client("target-1",
                permission("permission-1"),
                permission("permission-2")
        );
        var allowedClient2 = client("target-2",
                permission("permission-3")
        );

        var source = OAuthApplication.builder()
                .applicationType(ApplicationType.SERVICE)
                .name("client-1")
                .clientId("client-id-1")
                .clientSecret("client-secret-1")
                .audience("audience-1")
                .allowedClients(List.of(allowedClient1, allowedClient2))
                .callbacks(List.of(
                        callback("callback-1"),
                        callback("callback-2")
                ))
                .registeredPermissions(List.of(
                        permission("permission-4"),
                        permission("permission-5")
                ))
                .requiredPermissions(List.of(
                        permission("permission-6"),
                        permission("permission-7"),
                        permission("permission-8")
                ))
                .build();

        var expectedResult = OAuthClient.builder()
                .applicationType(ApplicationType.SERVICE)
                .clientName("client-1")
                .clientId("client-id-1")
                .clientSecret("client-secret-1")
                .audience("audience-1")
                .allowedClients(List.of(
                        relation("target-1", "permission-1", "permission-2"),
                        relation("target-2", "permission-3")
                ))
                .allowedCallbacks(List.of("callback-1", "callback-2"))
                .registeredScopes(List.of("permission-4", "permission-5"))
                .requiredScopes(List.of("permission-6", "permission-7", "permission-8"))
                .build();

        // when
        var result =  oAuthClientConverter.convert(source);

        // then
        assertThat(result, equalTo(expectedResult));
    }

    @Test
    public void shouldConvertMinimalObject() {

        // given
        var source = OAuthApplication.builder()
                .applicationType(ApplicationType.UI)
                .name("client-2")
                .clientId("client-id-2")
                .clientSecret("client-secret-2")
                .build();

        var expectedResult = OAuthClient.builder()
                .applicationType(ApplicationType.UI)
                .clientName("client-2")
                .clientId("client-id-2")
                .clientSecret("client-secret-2")
                .audience(null)
                .allowedClients(Collections.emptyList())
                .allowedCallbacks(Collections.emptyList())
                .registeredScopes(Collections.emptyList())
                .requiredScopes(Collections.emptyList())
                .build();

        // when
        var result =  oAuthClientConverter.convert(source);

        // then
        assertThat(result, equalTo(expectedResult));
    }

    private OAuthAllowedClient client(String name, Permission... permissions) {

        return OAuthAllowedClient.builder()
                .targetApplication(OAuthApplication.builder()
                        .name(name)
                        .build())
                .permissions(List.of(permissions))
                .build();
    }

    private Permission permission(String name) {

        return Permission.builder()
                .name(name)
                .build();
    }

    private OAuthCallback callback(String url) {

        return OAuthCallback.builder()
                .url(url)
                .build();
    }

    private OAuthClientAllowRelation relation(String name, String... permissions) {

        return OAuthClientAllowRelation.builder()
                .name(name)
                .allowedScopes(List.of(permissions))
                .build();
    }
}
