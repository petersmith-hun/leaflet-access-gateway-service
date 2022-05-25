package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link CommonTokenOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class CommonTokenOAuthRequestVerifierTest {

    @InjectMocks
    private CommonTokenOAuthRequestVerifier commonTokenOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthTokenRequestContext context = prepareContext("client-1", "audience-1", false);

        // when
        commonTokenOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', nullValues = "null", value = {
            "'';aud;client_id",
            "null;aud;client_id",
            "client;'';audience",
            "client;null;audience"
    })
    public void shouldVerifyRejectContextForMissingFields(String clientID, String audience, String expectedMissingFieldName) {

        // given
        OAuthTokenRequestContext context = prepareContext(clientID, audience, false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> commonTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", expectedMissingFieldName)));
    }

    @Test
    public void shouldVerifyRejectContextForInvalidScope() {

        // given
        OAuthTokenRequestContext context = prepareContext("client-1", "audience-1", true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> commonTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Target client [client-2] does not allow the requested scope [[read:other, write:admin]] for source client [client-1]"));
    }

    @Test
    public void shouldForGrantTypeReturnAllGrantTypes() {

        // when
        List<GrantType> result = commonTokenOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(3));
        assertThat(result, hasItems(GrantType.values()));
    }

    private OAuthTokenRequestContext prepareContext(String clientID, String audience, boolean withBroaderScope) {

        OAuthClientAllowRelation relation = OAuthConfigTestHelper.prepareRelation("client-2", Arrays.asList("read:users", "write:users", "read:admin", "write:admin"));
        OAuthClient sourceClient = OAuthConfigTestHelper.prepareOAuthClient("client-1", ApplicationType.SERVICE, clientID, null, null);
        OAuthClient targetClient = OAuthConfigTestHelper.prepareOAuthClient("client-2", ApplicationType.SERVICE, "client-2", null, null);

        return OAuthTokenRequestContext.builder()
                .relation(relation)
                .sourceClient(sourceClient)
                .targetClient(targetClient)
                .request(OAuthTokenRequest.builder()
                        .clientID(clientID)
                        .audience(audience)
                        .scope(withBroaderScope
                                ? List.of("read:other", "write:admin")
                                : List.of("read:users", "write:users"))
                        .build())
                .build();
    }
}
