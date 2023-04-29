package hu.psprog.leaflet.lags.core.service.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper;
import hu.psprog.leaflet.lags.core.domain.internal.AccessTokenInfo;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
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
import hu.psprog.leaflet.lags.core.domain.response.UserInfoResponse;
import hu.psprog.leaflet.lags.core.exception.JWTTokenParsingException;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.dao.AccessTokenDAO;
import hu.psprog.leaflet.lags.core.service.factory.OAuthRequestContextFactory;
import hu.psprog.leaflet.lags.core.service.processor.GrantFlowProcessor;
import hu.psprog.leaflet.lags.core.service.token.TokenHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
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

    private static final OAuthTokenRequest SUPPORTED_O_AUTH_TOKEN_REQUEST = prepareTokenRequest(GrantType.CLIENT_CREDENTIALS, "client-1");
    private static final OAuthTokenRequest UNSUPPORTED_O_AUTH_TOKEN_REQUEST = prepareTokenRequest(GrantType.PASSWORD, "client-2");
    private static final OAuthClient O_AUTH_CLIENT = prepareOAuthClient();
    private static final TokenClaims CLAIMS = TokenClaims.builder().audience("audience-1").build();
    private static final OAuthTokenResponse DUMMY_O_AUTH_TOKEN_RESPONSE = prepareTokenResponse();
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST = prepareAuthorizationRequest();
    private static final OAuthAuthorizationResponse DUMMY_O_AUTH_AUTHORIZATION_RESPONSE = prepareAuthorizationResponse();
    private static final String ACCESS_TOKEN = "jwt-token-1";
    private static final TokenClaims TOKEN_CLAIMS = prepareTokenClaims();
    private static final UserInfoResponse USER_INFO_RESPONSE = prepareUserInfoResponse();
    private static final OAuthAuthorizationRequestContext O_AUTH_AUTHORIZATION_REQUEST_CONTEXT = prepareAuthorizationContext();
    private static final OAuthTokenRequestContext O_AUTH_TOKEN_REQUEST_CONTEXT = prepareTokenContext();

    @Mock
    private GrantFlowProcessor grantFlowProcessor1;

    @Mock
    private GrantFlowProcessor grantFlowProcessor2;

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private AccessTokenDAO accessTokenDAO;

    @Mock
    private OAuthRequestContextFactory oAuthRequestContextFactory;

    private OAuthAuthorizationServiceImpl oAuthAuthorizationService;

    @BeforeEach
    public void setup() {

        given(grantFlowProcessor1.forGrantType()).willReturn(GrantType.CLIENT_CREDENTIALS);
        given(grantFlowProcessor2.forGrantType()).willReturn(GrantType.AUTHORIZATION_CODE);

        oAuthAuthorizationService = new OAuthAuthorizationServiceImpl(Arrays.asList(grantFlowProcessor1, grantFlowProcessor2),
                tokenHandler, accessTokenDAO, oAuthRequestContextFactory);
    }

    @Test
    public void shouldAuthorizeForCodeAuthProcessAuthorizationRequestSuccessfully() {

        // given
        given(oAuthRequestContextFactory.createContext(O_AUTH_AUTHORIZATION_REQUEST)).willReturn(O_AUTH_AUTHORIZATION_REQUEST_CONTEXT);
        given(grantFlowProcessor2.processAuthorizationRequest(O_AUTH_AUTHORIZATION_REQUEST_CONTEXT)).willReturn(DUMMY_O_AUTH_AUTHORIZATION_RESPONSE);

        // when
        OAuthAuthorizationResponse result = oAuthAuthorizationService.authorize(O_AUTH_AUTHORIZATION_REQUEST);

        // then
        assertThat(result, equalTo(DUMMY_O_AUTH_AUTHORIZATION_RESPONSE));
    }

    @Test
    public void shouldAuthorizeProcessRequestSuccessfully() {

        // given
        given(oAuthRequestContextFactory.createContext(SUPPORTED_O_AUTH_TOKEN_REQUEST)).willReturn(O_AUTH_TOKEN_REQUEST_CONTEXT);
        given(grantFlowProcessor1.processTokenRequest(O_AUTH_TOKEN_REQUEST_CONTEXT)).willReturn(CLAIMS);
        given(tokenHandler.generateToken(SUPPORTED_O_AUTH_TOKEN_REQUEST, CLAIMS)).willReturn(DUMMY_O_AUTH_TOKEN_RESPONSE);

        // when
        OAuthTokenResponse result = oAuthAuthorizationService.authorize(SUPPORTED_O_AUTH_TOKEN_REQUEST);

        // then
        assertThat(result, equalTo(DUMMY_O_AUTH_TOKEN_RESPONSE));
    }

    @Test
    public void shouldAuthorizeThrowExceptionForUnsupportedAuthFlow() {

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
        doThrow(JWTTokenParsingException.class).when(tokenHandler).parseToken(ACCESS_TOKEN);

        // when
        TokenIntrospectionResult result = oAuthAuthorizationService.introspect(ACCESS_TOKEN);

        // then
        assertThat(result.isActive(), is(false));
        assertThat(result.getClientID(), nullValue());
        assertThat(result.getUsername(), nullValue());
        assertThat(result.getExpiration(), nullValue());
        verifyNoInteractions(accessTokenDAO);
    }

    @Test
    public void shouldGetUserInfoReturnExtractedUserInformationFromToken() {

        // given
        given(tokenHandler.parseToken(ACCESS_TOKEN)).willReturn(TOKEN_CLAIMS);

        // when
        UserInfoResponse result = oAuthAuthorizationService.getUserInfo(ACCESS_TOKEN);

        // then
        assertThat(result, equalTo(USER_INFO_RESPONSE));
    }

    private Optional<AccessTokenInfo> prepareAccessTokenInfo(TokenStatus status) {

        AccessTokenInfo accessTokenInfo = null;
        if (Objects.nonNull(status)) {
            accessTokenInfo = new AccessTokenInfo(StoreAccessTokenInfoRequest.builder().build());
            accessTokenInfo.setStatus(status);
        }

        return Optional.ofNullable(accessTokenInfo);
    }

    private static OAuthTokenRequest prepareTokenRequest(GrantType grantType, String clientID) {

        return OAuthTokenRequest.builder()
                .grantType(grantType)
                .clientID(clientID)
                .build();
    }

    private static OAuthClient prepareOAuthClient() {
        return OAuthConfigTestHelper.prepareOAuthClient("Client 1", ApplicationType.SERVICE, "client-1", null, null);
    }

    private static OAuthTokenResponse prepareTokenResponse() {

        return OAuthTokenResponse.builder()
                .accessToken("token-1")
                .build();
    }

    private static OAuthAuthorizationRequest prepareAuthorizationRequest() {

        return OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.CODE)
                .clientID("client-1")
                .build();
    }

    private static OAuthAuthorizationResponse prepareAuthorizationResponse() {

        return OAuthAuthorizationResponse.builder()
                .code("code-1")
                .build();
    }

    private static TokenClaims prepareTokenClaims() {

        return TokenClaims.builder()
                .tokenID(UUID.randomUUID().toString())
                .clientID("client-1")
                .username("username-1")
                .email("email@dev.local")
                .userID(1234L)
                .expiration(new Date())
                .build();
    }

    private static UserInfoResponse prepareUserInfoResponse() {

        return UserInfoResponse.builder()
                .sub("1234")
                .email("email@dev.local")
                .name("username-1")
                .build();
    }

    private static OAuthAuthorizationRequestContext prepareAuthorizationContext() {

        return OAuthAuthorizationRequestContext.builder()
                .request(O_AUTH_AUTHORIZATION_REQUEST)
                .sourceClient(O_AUTH_CLIENT)
                .build();
    }

    private static OAuthTokenRequestContext prepareTokenContext() {

        return OAuthTokenRequestContext.builder()
                .request(SUPPORTED_O_AUTH_TOKEN_REQUEST)
                .sourceClient(O_AUTH_CLIENT)
                .build();
    }
}
