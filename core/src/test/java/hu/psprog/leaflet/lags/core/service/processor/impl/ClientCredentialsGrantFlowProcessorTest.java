package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static hu.psprog.leaflet.lags.core.service.processor.impl.GrantFlowProcessorTestHelper.INVALID_TARGET_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.service.processor.impl.GrantFlowProcessorTestHelper.SOURCE_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.service.processor.impl.GrantFlowProcessorTestHelper.TARGET_O_AUTH_CLIENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link ClientCredentialsGrantFlowProcessor}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ClientCredentialsGrantFlowProcessorTest {

    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest();

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @InjectMocks
    private ClientCredentialsGrantFlowProcessor clientCredentialsGrantFlowProcessor;

    @Test
    public void shouldVerifyRequestGenerateClaimsWithSuccess() {

        // given
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Map<String, Object> result = clientCredentialsGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT);

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, equalTo(Map.of(
                "scope", "read:items write:item:self",
                "sub", "dummy-source-service-1"
        )));
    }

    @ParameterizedTest
    @MethodSource("missingFieldDataProvider")
    public void shouldVerifyRequestThrowExceptionIfMandatoryFieldsAreMissing(String missingFieldName, OAuthTokenRequest oAuthTokenRequest) {

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsGrantFlowProcessor.verifyRequest(oAuthTokenRequest, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", missingFieldName)));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfTargetClientIsNotRegistered() {

        // given
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Requested access for non-registered OAuth client [target-service-audience]"));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfAccessingTargetIsNotAllowedToSourceClient() {

        // given
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(INVALID_TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Target client [target-service-1] does not allow access for source client [source-service-1]"));
    }

    @ParameterizedTest
    @MethodSource("requestWithInvalidScopeDataProvider")
    public void shouldVerifyRequestThrowExceptionIfSourceRequestedNotAllowedScope(OAuthTokenRequest oAuthTokenRequest) {

        // given
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsGrantFlowProcessor.verifyRequest(oAuthTokenRequest, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Target client [target-service-1] does not allow the requested scope [%s] for source client [source-service-1]",
                oAuthTokenRequest.getScope())));
    }

    @Test
    public void shouldForGrantTypeReturnClientCredentials() {

        // when
        GrantType result = clientCredentialsGrantFlowProcessor.forGrantType();

        // then
        assertThat(result, equalTo(GrantType.CLIENT_CREDENTIALS));
    }

    private static OAuthTokenRequest prepareValidOAuthTokenRequest() {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .audience(TARGET_O_AUTH_CLIENT.getAudience())
                .scope(Arrays.asList("read:items", "write:item:self"))
                .build();
    }

    private static OAuthTokenRequest prepareOAuthTokenRequest(String... scope) {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.CLIENT_CREDENTIALS)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .audience(TARGET_O_AUTH_CLIENT.getAudience())
                .scope(Arrays.asList(scope))
                .build();
    }

    private static Stream<Arguments> missingFieldDataProvider() {

        return Stream.of(
                Arguments.of("client_id", OAuthTokenRequest.builder().audience("audience1").scope(Collections.singletonList("read:all")).build()),
                Arguments.of("audience", OAuthTokenRequest.builder().clientID("client1").scope(Collections.singletonList("read:all")).build()),
                Arguments.of("scope", OAuthTokenRequest.builder().clientID("client1").audience("audience1").scope(null).build()),
                Arguments.of("scope", OAuthTokenRequest.builder().clientID("client1").audience("audience1").scope(Collections.emptyList()).build())
        );
    }

    private static Stream<Arguments> requestWithInvalidScopeDataProvider() {

        return Stream.of(
                Arguments.of(prepareOAuthTokenRequest("admin:item")),
                Arguments.of(prepareOAuthTokenRequest("admin:item", "read:items", "write:item:self")),
                Arguments.of(prepareOAuthTokenRequest("some:non:existing", "read:items"))
        );
    }
}
