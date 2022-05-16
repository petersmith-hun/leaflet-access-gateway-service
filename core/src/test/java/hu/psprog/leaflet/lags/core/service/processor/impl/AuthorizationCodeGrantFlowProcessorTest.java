package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.UserInfo;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AuthorizationCodeGrantFlowProcessor}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationCodeGrantFlowProcessorTest {

    private static final String SOURCE_CLIENT_ID = "client-1";
    private static final String TARGET_CLIENT_AUDIENCE = "client-2-aud";
    private static final String VALID_REDIRECT_URI = "https://dev.local:9999/callback";
    private static final String AUTHORIZATION_CODE = "auth-code-1";

    private static final OAuthClient SOURCE_O_AUTH_CLIENT = prepareSourceClient(true);
    private static final OAuthClient SOURCE_O_AUTH_CLIENT_NON_UI = prepareSourceClient(false);
    private static final OAuthClient TARGET_O_AUTH_CLIENT = prepareTargetClient();
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST = prepareAuthorizationRequest(false);
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST_WITH_SCOPE = prepareAuthorizationRequest(true);
    private static final ExtendedUser EXTENDED_USER = prepareExtendedUser(true);
    private static final UserInfo USER_INFO = UserInfo.builder()
            .id(EXTENDED_USER.getId())
            .email(EXTENDED_USER.getUsername())
            .username(EXTENDED_USER.getName())
            .role(EXTENDED_USER.getRole())
            .build();
    private static final ExtendedUser EXTENDED_USER_WITH_LACK_OF_MANDATORY_SCOPES = prepareExtendedUser(false);
    private static final OngoingAuthorization ONGOING_AUTHORIZATION = prepareOngoingAuthorization();
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST = prepareOAuthTokenRequest(false);
    private static final OAuthTokenRequest O_AUTH_TOKEN_REQUEST_WITH_DIFFERENT_SCOPE = prepareOAuthTokenRequest(true);

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private OngoingAuthorization mockedOngoingAuthorization;

    @Captor
    private ArgumentCaptor<OngoingAuthorization> ongoingAuthorizationArgumentCaptor;

    @InjectMocks
    private AuthorizationCodeGrantFlowProcessor authorizationCodeGrantFlowProcessor;

    @Test
    public void shouldAuthorizeRequestCreateAuthorizationResponseWithDefaultScope() {

        // given
        given(authentication.getPrincipal()).willReturn(EXTENDED_USER);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        OAuthAuthorizationResponse result = authorizationCodeGrantFlowProcessor.authorizeRequest(O_AUTH_AUTHORIZATION_REQUEST, SOURCE_O_AUTH_CLIENT);

        // then
        assertThat(result.getRedirectURI(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getRedirectURI()));
        assertThat(result.getState(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getState()));
        verifyOngoingAuthorization(result, false);
    }

    @Test
    public void shouldAuthorizeRequestCreateAuthorizationResponseWithRequestedScope() {

        // given
        given(authentication.getPrincipal()).willReturn(EXTENDED_USER);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        OAuthAuthorizationResponse result = authorizationCodeGrantFlowProcessor.authorizeRequest(O_AUTH_AUTHORIZATION_REQUEST_WITH_SCOPE, SOURCE_O_AUTH_CLIENT);

        // then
        assertThat(result.getRedirectURI(), equalTo(O_AUTH_AUTHORIZATION_REQUEST_WITH_SCOPE.getRedirectURI()));
        assertThat(result.getState(), equalTo(O_AUTH_AUTHORIZATION_REQUEST_WITH_SCOPE.getState()));
        verifyOngoingAuthorization(result, true);
    }

    @Test
    public void shouldAuthorizeRequestThrowExceptionOnInvalidRequestedResponseType() {

        // given
        OAuthAuthorizationRequest request = prepareAuthorizationRequest(true, false, true, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.authorizeRequest(request, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Authorization response type must be [code]"));
    }

    @Test
    public void shouldAuthorizeRequestThrowExceptionOnInvalidRequestedApplicationType() {

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.authorizeRequest(O_AUTH_AUTHORIZATION_REQUEST, SOURCE_O_AUTH_CLIENT_NON_UI));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client application is not permitted to use authorization code flow."));
    }

    @Test
    public void shouldAuthorizeRequestThrowExceptionOnInvalidRequestedRedirectURI() {

        // given
        OAuthAuthorizationRequest request = prepareAuthorizationRequest(true, true, false, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.authorizeRequest(request, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Specified redirection URI [https://dev.local:8888/invalid-callback] is not registered"));
    }

    @Test
    public void shouldAuthorizeRequestThrowExceptionOnInvalidRequestedScope() {

        // given
        OAuthAuthorizationRequest request = prepareAuthorizationRequest(true, true, true, false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.authorizeRequest(request, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Requested scope is broader than the user's authority range."));
    }

    @Test
    public void shouldAuthorizeRequestThrowExceptionForLackOfMandatoryScopes() {

        // given
        given(authentication.getPrincipal()).willReturn(EXTENDED_USER_WITH_LACK_OF_MANDATORY_SCOPES);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.authorizeRequest(O_AUTH_AUTHORIZATION_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client requires broader authorities than what the user has."));
    }

    @Test
    public void shouldVerifyRequestGenerateClaimsWithSuccess() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(ONGOING_AUTHORIZATION));
        given(oAuthClientRegistry.getClientByAudience(TARGET_CLIENT_AUDIENCE)).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Map<String, Object> result = authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT);

        // then
        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
        assertThat(result.size(), equalTo(6));
        assertThat(result, equalTo(Map.of(
                "sub", "client-1|uid=1234",
                "usr", USER_INFO.getEmail(),
                "rol", USER_INFO.getRole(),
                "name", USER_INFO.getUsername(),
                "uid", USER_INFO.getId(),
                "scope", String.join(StringUtils.SPACE, SOURCE_O_AUTH_CLIENT.getRegisteredScopes())
        )));
    }

    @ParameterizedTest
    @MethodSource("missingFieldDataProvider")
    public void shouldVerifyRequestThrowExceptionIfMandatoryFieldsAreMissing(String missingFieldName, OAuthTokenRequest oAuthTokenRequest) {

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(oAuthTokenRequest, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", missingFieldName)));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfOngoingAuthorizationIsMissing() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Unknown authorization request"));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfClientIDIsDifferent() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(mockedOngoingAuthorization));
        given(mockedOngoingAuthorization.getClientID()).willReturn("some-different-client-id");

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Authorization request belongs to a different client."));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfRedirectURIIsDifferent() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(mockedOngoingAuthorization));
        given(mockedOngoingAuthorization.getClientID()).willReturn(SOURCE_CLIENT_ID);
        given(mockedOngoingAuthorization.getRedirectURI()).willReturn("https://dev.local:7777/invalid-callback");

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Different redirect URI has been specified in the token request."));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfAuthorizationRequestIsExpired() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(mockedOngoingAuthorization));
        given(mockedOngoingAuthorization.getAuthorizationCode()).willReturn(AUTHORIZATION_CODE);
        given(mockedOngoingAuthorization.getClientID()).willReturn(SOURCE_CLIENT_ID);
        given(mockedOngoingAuthorization.getRedirectURI()).willReturn(VALID_REDIRECT_URI);
        given(mockedOngoingAuthorization.getExpiration()).willReturn(LocalDateTime.now().minusMinutes(2L));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
        assertThat(result.getMessage(), equalTo("Authorization has already expired."));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfDifferentScopeIsRequested() {

        // given
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(ONGOING_AUTHORIZATION));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.verifyRequest(O_AUTH_TOKEN_REQUEST_WITH_DIFFERENT_SCOPE, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Token request should not specify scope on Authorization Code flow."));
    }

    @Test
    public void shouldForGrantTypeReturnAuthorizationCode() {

        // when
        GrantType result = authorizationCodeGrantFlowProcessor.forGrantType();

        // then
        assertThat(result, equalTo(GrantType.AUTHORIZATION_CODE));
    }

    private void verifyOngoingAuthorization(OAuthAuthorizationResponse result, boolean withRequestedScope) {

        verify(ongoingAuthorizationRepository).saveOngoingAuthorization(ongoingAuthorizationArgumentCaptor.capture());

        OngoingAuthorization ongoingAuthorization = ongoingAuthorizationArgumentCaptor.getValue();
        assertThat(ongoingAuthorization.getAuthorizationCode(), equalTo(result.getCode()));
        assertThat(ongoingAuthorization.getClientID(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getClientID()));
        assertThat(ongoingAuthorization.getRedirectURI(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getRedirectURI()));
        assertThat(ongoingAuthorization.getUserInfo(), equalTo(USER_INFO));
        assertThat(ongoingAuthorization.getScope(), equalTo(withRequestedScope
                ? Arrays.asList("write:admin", "write:users")
                : Arrays.asList("read:users", "write:users", "read:admin", "write:admin", "write:entries")));

        long expirationInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), ongoingAuthorization.getExpiration());
        assertThat(expirationInSeconds > 57 && expirationInSeconds <= 60, is(true));
    }

    private static OAuthClient prepareSourceClient(boolean isUIClient) {

        return new OAuthClient(
                "ui-client",
                isUIClient ? ApplicationType.UI : ApplicationType.SERVICE,
                SOURCE_CLIENT_ID,
                "client-secret-1",
                "client-1-aud",
                Collections.emptyList(),
                Arrays.asList("read:users", "write:users", "read:admin", "write:admin"),
                Collections.emptyList(),
                Collections.singletonList(VALID_REDIRECT_URI));
    }

    private static OAuthClient prepareTargetClient() {

        OAuthClientAllowRelation relation = new OAuthClientAllowRelation("ui-client", Arrays.asList("read:users", "write:users", "read:admin", "write:admin"));

        return new OAuthClient(
                "some-service",
                ApplicationType.SERVICE,
                "client-2",
                "client-secret-2",
                TARGET_CLIENT_AUDIENCE,
                Arrays.asList("read:users", "write:users", "read:admin", "write:admin", "write:other1", "write:other2"),
                Collections.emptyList(),
                Collections.singletonList(relation),
                Collections.emptyList());
    }

    private static OAuthAuthorizationRequest prepareAuthorizationRequest(boolean withRequestedScope) {
        return prepareAuthorizationRequest(withRequestedScope, true, true, true);
    }

    private static OAuthAuthorizationRequest prepareAuthorizationRequest(boolean withRequestedScope, boolean withValidResponseType,
                                                                         boolean withValidRedirectURI, boolean withValidScope) {

        return OAuthAuthorizationRequest.builder()
                .responseType(withValidResponseType ? AuthorizationResponseType.CODE : null)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .redirectURI(withValidRedirectURI ? SOURCE_O_AUTH_CLIENT.getAllowedCallbacks().get(0) : "https://dev.local:8888/invalid-callback")
                .state("state-1")
                .scope(withRequestedScope
                        ? withValidScope
                            ? "write:admin write:users"
                            : "some:random:scope"
                        : null)
                .build();
    }

    private static ExtendedUser prepareExtendedUser(boolean withAllMandatoryScopes) {

        return ExtendedUser.builder()
                .id(1234L)
                .username("email@dev.local")
                .name("User 1")
                .role("ADMIN")
                .enabled(true)
                .authorities(withAllMandatoryScopes
                        ? AuthorityUtils.createAuthorityList("read:users", "write:users", "read:admin", "write:admin", "write:entries")
                        : AuthorityUtils.createAuthorityList("read:users", "read:admin"))
                .build();
    }

    private static OngoingAuthorization prepareOngoingAuthorization() {

        return OngoingAuthorization.builder()
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID(O_AUTH_AUTHORIZATION_REQUEST.getClientID())
                .redirectURI(O_AUTH_AUTHORIZATION_REQUEST.getRedirectURI())
                .userInfo(USER_INFO)
                .expiration(LocalDateTime.now().plusMinutes(1L))
                .scope(SOURCE_O_AUTH_CLIENT.getRegisteredScopes())
                .build();
    }

    private static OAuthTokenRequest prepareOAuthTokenRequest(boolean withScope) {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.AUTHORIZATION_CODE)
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID(O_AUTH_AUTHORIZATION_REQUEST.getClientID())
                .audience(TARGET_CLIENT_AUDIENCE)
                .scope(withScope
                        ? Arrays.asList("read:users", "write:users")
                        : new LinkedList<>())
                .redirectURI(VALID_REDIRECT_URI)
                .build();
    }

    private static Stream<Arguments> missingFieldDataProvider() {

        return Stream.of(
                Arguments.of("client_id", OAuthTokenRequest.builder().audience(TARGET_CLIENT_AUDIENCE).authorizationCode(AUTHORIZATION_CODE).redirectURI(VALID_REDIRECT_URI).build()),
                Arguments.of("audience", OAuthTokenRequest.builder().clientID(SOURCE_CLIENT_ID).authorizationCode(AUTHORIZATION_CODE).redirectURI(VALID_REDIRECT_URI).build()),
                Arguments.of("code", OAuthTokenRequest.builder().clientID(SOURCE_CLIENT_ID).audience(TARGET_CLIENT_AUDIENCE).redirectURI(VALID_REDIRECT_URI).build()),
                Arguments.of("redirect_uri", OAuthTokenRequest.builder().clientID(SOURCE_CLIENT_ID).audience(TARGET_CLIENT_AUDIENCE).authorizationCode(AUTHORIZATION_CODE).build())
        );
    }
}
