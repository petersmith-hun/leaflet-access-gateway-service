package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.StoreAccessTokenInfoRequest;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.internal.TokenStatus;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthTokenResponse;
import hu.psprog.leaflet.lags.core.domain.response.TokenIntrospectionResult;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;

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
            .grantType(GrantType.PASSWORD)
            .clientID("client-2")
            .build();
    private static final OAuthClient O_AUTH_CLIENT = new OAuthClient("Client 1", ApplicationType.SERVICE, "client-1", null, null, null, null, null,null);
    private static final Map<String, Object> CLAIMS = Map.of("aud", "audience-1");
    private static final OAuthTokenResponse DUMMY_O_AUTH_TOKEN_RESPONSE = OAuthTokenResponse.builder()
            .accessToken("token-1")
            .build();
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST = OAuthAuthorizationRequest.builder()
            .responseType(AuthorizationResponseType.CODE)
            .clientID("client-1")
            .build();
    private static final OAuthAuthorizationResponse DUMMY_O_AUTH_AUTHORIZATION_RESPONSE = OAuthAuthorizationResponse.builder()
            .code("code-1")
            .build();
    private static final String ACCESS_TOKEN = "jwt-token-1";
    private static final TokenClaims TOKEN_CLAIMS = TokenClaims.builder()
            .tokenID(UUID.randomUUID().toString())
            .clientID("client-1")
            .username("username-1")
            .expiration(new Date())
            .build();

    @Mock
    private GrantFlowProcessor grantFlowProcessor1;

    @Mock
    private GrantFlowProcessor grantFlowProcessor2;

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private AccessTokenDAO accessTokenDAO;

    private OAuthAuthorizationServiceImpl oAuthAuthorizationService;

    @BeforeEach
    public void setup() {

        given(grantFlowProcessor1.forGrantType()).willReturn(GrantType.CLIENT_CREDENTIALS);
        given(grantFlowProcessor2.forGrantType()).willReturn(GrantType.AUTHORIZATION_CODE);

        oAuthAuthorizationService = new OAuthAuthorizationServiceImpl(Arrays.asList(grantFlowProcessor1, grantFlowProcessor2),
                oAuthClientRegistry, tokenHandler, accessTokenDAO);
    }

    @Test
    public void shouldAuthorizeForCodeAuthProcessAuthorizationRequestSuccessfully() {

        // given
        given(oAuthClientRegistry.getClientByClientID(O_AUTH_AUTHORIZATION_REQUEST.getClientID())).willReturn(Optional.of(O_AUTH_CLIENT));
        given(grantFlowProcessor2.authorizeRequest(O_AUTH_AUTHORIZATION_REQUEST, O_AUTH_CLIENT)).willReturn(DUMMY_O_AUTH_AUTHORIZATION_RESPONSE);

        // when
        OAuthAuthorizationResponse result = oAuthAuthorizationService.authorize(O_AUTH_AUTHORIZATION_REQUEST);

        // then
        assertThat(result, equalTo(DUMMY_O_AUTH_AUTHORIZATION_RESPONSE));
    }

    @Test
    public void shouldAuthorizeForCodeAuthThrowExceptionForUnknownClient() {

        // given
        given(oAuthClientRegistry.getClientByClientID(SUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationService.authorize(O_AUTH_AUTHORIZATION_REQUEST));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth client by ID [client-1] is not registered"));
    }

    @Test
    public void shouldAuthorizeProcessRequestSuccessfully() {

        // given
        given(oAuthClientRegistry.getClientByClientID(SUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.of(O_AUTH_CLIENT));
        given(grantFlowProcessor1.verifyRequest(SUPPORTED_O_AUTH_TOKEN_REQUEST, O_AUTH_CLIENT)).willReturn(CLAIMS);
        given(tokenHandler.generateToken(SUPPORTED_O_AUTH_TOKEN_REQUEST, CLAIMS)).willReturn(DUMMY_O_AUTH_TOKEN_RESPONSE);

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
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationService.authorize(SUPPORTED_O_AUTH_TOKEN_REQUEST));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth client by ID [client-1] is not registered"));
    }

    @Test
    public void shouldAuthorizeThrowExceptionForUnsupportedAuthFlow() {

        // given
        given(oAuthClientRegistry.getClientByClientID(UNSUPPORTED_O_AUTH_TOKEN_REQUEST.getClientID())).willReturn(Optional.of(O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthAuthorizationService.authorize(UNSUPPORTED_O_AUTH_TOKEN_REQUEST));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth authorization flow [PASSWORD] is not supported"));
    }

    @Test
    public void shouldIntrospectReturnIntrospectionResultWithActiveStatusFlag() {

        // given
        given(tokenHandler.parseToken(ACCESS_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(accessTokenDAO.retrieveByJTI(TOKEN_CLAIMS.getTokenID())).willReturn(prepareAccessTokenInfo(TokenStatus.ACTIVE));

        // when
        TokenIntrospectionResult result = oAuthAuthorizationService.introspect(ACCESS_TOKEN);

        // then
        assertThat(result.isActive(), is(true));
        assertThat(result.getClientID(), equalTo(TOKEN_CLAIMS.getClientID()));
        assertThat(result.getUsername(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(result.getExpiration(), equalTo(TOKEN_CLAIMS.getExpiration()));
    }

    @Test
    public void shouldIntrospectReturnIntrospectionResultWithInactiveStatusFlagDueToMissingTrackingInfo() {

        // given
        given(tokenHandler.parseToken(ACCESS_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(accessTokenDAO.retrieveByJTI(TOKEN_CLAIMS.getTokenID())).willReturn(prepareAccessTokenInfo(null));

        // when
        TokenIntrospectionResult result = oAuthAuthorizationService.introspect(ACCESS_TOKEN);

        // then
        assertThat(result.isActive(), is(false));
        assertThat(result.getClientID(), equalTo(TOKEN_CLAIMS.getClientID()));
        assertThat(result.getUsername(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(result.getExpiration(), equalTo(TOKEN_CLAIMS.getExpiration()));
    }

    @Test
    public void shouldIntrospectReturnIntrospectionResultWithInactiveStatusFlagDueToRevokedStatusInTrackingInfo() {

        // given
        given(tokenHandler.parseToken(ACCESS_TOKEN)).willReturn(TOKEN_CLAIMS);
        given(accessTokenDAO.retrieveByJTI(TOKEN_CLAIMS.getTokenID())).willReturn(prepareAccessTokenInfo(TokenStatus.REVOKED));

        // when
        TokenIntrospectionResult result = oAuthAuthorizationService.introspect(ACCESS_TOKEN);

        // then
        assertThat(result.isActive(), is(false));
        assertThat(result.getClientID(), equalTo(TOKEN_CLAIMS.getClientID()));
        assertThat(result.getUsername(), equalTo(TOKEN_CLAIMS.getUsername()));
        assertThat(result.getExpiration(), equalTo(TOKEN_CLAIMS.getExpiration()));
    }

    @Test
    public void shouldIntrospectReturnIntrospectionResultWithInactiveStatusOnInvalidToken() {

        // given
        doThrow(JwtException.class).when(tokenHandler).parseToken(ACCESS_TOKEN);

        // when
        TokenIntrospectionResult result = oAuthAuthorizationService.introspect(ACCESS_TOKEN);

        // then
        assertThat(result.isActive(), is(false));
        assertThat(result.getClientID(), nullValue());
        assertThat(result.getUsername(), nullValue());
        assertThat(result.getExpiration(), nullValue());
        verifyNoInteractions(accessTokenDAO);
    }

    private Optional<AccessTokenInfo> prepareAccessTokenInfo(TokenStatus status) {

        AccessTokenInfo accessTokenInfo = null;
        if (Objects.nonNull(status)) {
            accessTokenInfo = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().build());
            accessTokenInfo.setStatus(status);
        }

        return Optional.ofNullable(accessTokenInfo);
    }
}
