package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.SOURCE_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.TARGET_O_AUTH_CLIENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ClientCredentialsGrantFlowProcessor}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ClientCredentialsGrantFlowProcessorTest {

    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest();

    @Mock
    private OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier1;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier2;

    @InjectMocks
    private ClientCredentialsGrantFlowProcessor clientCredentialsGrantFlowProcessor;

    @Test
    public void shouldProcessTokenRequestGenerateClaimsWithSuccess() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.CLIENT_CREDENTIALS)).willReturn(List.of(verifier1, verifier2));

        // when
        Map<String, Object> result = clientCredentialsGrantFlowProcessor.processTokenRequest(context);

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, equalTo(Map.of(
                "scope", "read:items write:item:self",
                "sub", "dummy-source-service-1"
        )));
        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.CLIENT_CREDENTIALS);
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
    }

    @Test
    public void shouldProcessTokenRequestPassUpTheExceptionIfAVerifierFails() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.CLIENT_CREDENTIALS)).willReturn(List.of(verifier1, verifier2));
        doThrow(OAuthAuthorizationException.class).when(verifier2).verify(context);

        // when
        assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsGrantFlowProcessor.processTokenRequest(context));

        // then
        // exception expected

        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.CLIENT_CREDENTIALS);
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
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

}
