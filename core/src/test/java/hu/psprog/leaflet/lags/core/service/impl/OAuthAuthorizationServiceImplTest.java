package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.token.TokenGenerator;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuthAuthorizationServiceImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthAuthorizationServiceImplTest {

    private static final OAuthTokenRequest SUPPORTED_O_AUTH_TOKEN_REQUEST = OAuthTokenRequest.builder()
            .grantType(GrantType.CLIENT_CREDENTIALS)
            .clientID("client-1")
            .build();
    private static final OAuthTokenRequest UNSUPPORTED_O_AUTH_TOKEN_REQUEST = OAuthTokenRequest.builder()
            .grantType(GrantType.AUTHORIZATION_CODE)
            .clientID("client-2")
            .build();
    private static final OAuthClient O_AUTH_CLIENT = new OAuthClient("Client 1", "client-1", null, null, null, null);
    private static final Map<String, Object> CLAIMS = Map.of("aud", "audience-1");
    private static final OAuthTokenResponse DUMMY_O_AUTH_TOKEN_RESPONSE = OAuthTokenResponse.builder()
            .accessToken("token-1")
            .build();

    @Mock
    private GrantFlowProcessor grantFlowProcessor1;

    @Mock
    private GrantFlowProcessor grantFlowProcessor2;

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private TokenGenerator tokenGenerator;

    private OAuthAuthorizationServiceImpl oAuthAuthorizationService;

    @BeforeEach
    public void setup() {

        given(grantFlowProcessor1.forGrantType()).willReturn(GrantType.PASSWORD);
        given(grantFlowProcessor2.forGrantType()).willReturn(GrantType.CLIENT_CREDENTIALS);

        oAuthAuthorizationService = new OAuthAuthorizationServiceImpl(Arrays.asList(grantFlowProcessor1, grantFlowProcessor2),
                oAuthClientRegistry, tokenGenerator);
    }

    @Test
    public void shouldAuthorizeProcessRequestSuccessfully() {

        // given
        given(oAuthClientRegistry.getClientByClientID(SUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.of(O_AUTH_CLIENT));
        given(grantFlowProcessor2.verifyRequest(SUPPORTED_O_AUTH_TOKEN_REQUEST, O_AUTH_CLIENT)).willReturn(CLAIMS);
        given(tokenGenerator.generateToken(SUPPORTED_O_AUTH_TOKEN_REQUEST, CLAIMS)).willReturn(DUMMY_O_AUTH_TOKEN_RESPONSE);

        // when
        OAuthTokenResponse result = oAuthAuthorizationService.authorize(SUPPORTED_O_AUTH_TOKEN_REQUEST);

        // then
        assertThat(result, equalTo(DUMMY_O_AUTH_TOKEN_RESPONSE));
    }

    @Test
    public void shouldAuthorizeThrowExceptionForUnknownClient() {

        // given
        given(oAuthClientRegistry.getClientByClientID(SUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.empty());

        // when
        assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationService.authorize(SUPPORTED_O_AUTH_TOKEN_REQUEST),
                "OAuth client by ID [client-1] is not registered");

        // then
        // exception expected
    }

    @Test
    public void shouldAuthorizeThrowExceptionForUnsupportedAuthFlow() {

        // given
        given(oAuthClientRegistry.getClientByClientID(UNSUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.of(O_AUTH_CLIENT));

        // when
        assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationService.authorize(UNSUPPORTED_O_AUTH_TOKEN_REQUEST),
                "OAuth authorization flow [code] is not supported");

        // then
        // exception expected
    }
}
