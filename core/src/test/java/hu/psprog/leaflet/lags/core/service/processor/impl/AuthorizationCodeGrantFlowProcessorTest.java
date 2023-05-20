package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.internal.UserInfo;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthAuthorizationResponse;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.factory.OngoingAuthorizationFactory;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import hu.psprog.leaflet.lags.core.service.util.ScopeNegotiator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

    private static final OAuthClient SOURCE_O_AUTH_CLIENT = prepareSourceClient();
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST = prepareAuthorizationRequest();
    private static final ExtendedUser EXTENDED_USER = prepareExtendedUser();
    private static final UserInfo USER_INFO = prepareUserInfo();
    private static final OngoingAuthorization ONGOING_AUTHORIZATION = prepareOngoingAuthorization();

    @Mock
    private OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry;

    @Mock
    private OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @Mock
    private OngoingAuthorizationFactory ongoingAuthorizationFactory;

    @Mock
    private ScopeNegotiator scopeNegotiator;

    @Mock
    private OAuthRequestVerifier<OAuthAuthorizationRequestContext> verifier1;

    @Mock
    private OAuthRequestVerifier<OAuthAuthorizationRequestContext> verifier2;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier3;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier4;

    @InjectMocks
    private AuthorizationCodeGrantFlowProcessor authorizationCodeGrantFlowProcessor;

    @Test
    public void shouldProcessAuthorizationRequestCreateAuthorizationResponse() {

        // given
        OAuthAuthorizationRequestContext context = OAuthAuthorizationRequestContext.builder()
                .request(O_AUTH_AUTHORIZATION_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(ongoingAuthorizationFactory.createOngoingAuthorization(context)).willReturn(ONGOING_AUTHORIZATION);
        given(oAuthRequestVerifierRegistry.getAuthorizationRequestVerifiers()).willReturn(List.of(verifier1, verifier2));

        // when
        OAuthAuthorizationResponse result = authorizationCodeGrantFlowProcessor.processAuthorizationRequest(context);

        // then
        assertThat(result.redirectURI(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getRedirectURI()));
        assertThat(result.state(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getState()));
        assertThat(result.code(), equalTo(AUTHORIZATION_CODE));

        verify(ongoingAuthorizationRepository).saveOngoingAuthorization(ONGOING_AUTHORIZATION);
        verify(oAuthRequestVerifierRegistry).getAuthorizationRequestVerifiers();
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
    }

    @Test
    public void shouldProcessAuthorizationRequestPassUpTheExceptionIfAVerifierFails() {

        // given
        OAuthAuthorizationRequestContext context = OAuthAuthorizationRequestContext.builder()
                .request(O_AUTH_AUTHORIZATION_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(oAuthRequestVerifierRegistry.getAuthorizationRequestVerifiers()).willReturn(List.of(verifier1, verifier2));
        doThrow(OAuthAuthorizationException.class).when(verifier2).verify(context);

        // when
        assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.processAuthorizationRequest(context));

        // then
        // exception expected

        verify(ongoingAuthorizationRepository, never()).saveOngoingAuthorization(ONGOING_AUTHORIZATION);
        verify(oAuthRequestVerifierRegistry).getAuthorizationRequestVerifiers();
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
    }

    @Test
    public void shouldProcessTokenRequestGenerateClaimsWithSuccess() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(prepareOAuthTokenRequest())
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .ongoingAuthorization(Optional.of(ONGOING_AUTHORIZATION))
                .build();

        given(scopeNegotiator.getScope(context)).willReturn(SOURCE_O_AUTH_CLIENT.getRegisteredScopes());
        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.AUTHORIZATION_CODE)).willReturn(List.of(verifier3, verifier4));

        // when
        TokenClaims result = authorizationCodeGrantFlowProcessor.processTokenRequest(context);

        // then
        assertThat(result.getClaimsAsMap().size(), equalTo(6));
        assertThat(result.getClaimsAsMap(), equalTo(Map.of(
                "sub", "client-1|uid=1234",
                "usr", USER_INFO.getEmail(),
                "rol", USER_INFO.getRole(),
                "name", USER_INFO.getUsername(),
                "uid", USER_INFO.getId(),
                "scope", String.join(StringUtils.SPACE, SOURCE_O_AUTH_CLIENT.getRegisteredScopes())
        )));

        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.AUTHORIZATION_CODE);
        verify(verifier3).verify(context);
        verify(verifier4).verify(context);
    }

    @Test
    public void shouldProcessTokenRequestPassUpTheExceptionIfAVerifierFails() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(prepareOAuthTokenRequest())
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .ongoingAuthorization(Optional.of(ONGOING_AUTHORIZATION))
                .build();

        given(scopeNegotiator.getScope(context)).willReturn(SOURCE_O_AUTH_CLIENT.getRegisteredScopes());
        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.AUTHORIZATION_CODE)).willReturn(List.of(verifier3, verifier4));
        doThrow(OAuthAuthorizationException.class).when(verifier4).verify(context);

        // when
        assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeGrantFlowProcessor.processTokenRequest(context));

        // then
        // exception expected

        verify(ongoingAuthorizationRepository, never()).deleteOngoingAuthorization(AUTHORIZATION_CODE);
        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.AUTHORIZATION_CODE);
        verify(verifier3).verify(context);
        verify(verifier4).verify(context);
    }

    @Test
    public void shouldForGrantTypeReturnAuthorizationCode() {

        // when
        GrantType result = authorizationCodeGrantFlowProcessor.forGrantType();

        // then
        assertThat(result, equalTo(GrantType.AUTHORIZATION_CODE));
    }

    private static OAuthClient prepareSourceClient() {

        OAuthClient oAuthClient = OAuthConfigTestHelper.prepareOAuthClient(
                "ui-client",
                ApplicationType.UI,
                SOURCE_CLIENT_ID,
                "client-secret-1",
                "client-1-aud");

        OAuthConfigTestHelper.setRegisteredScopes(oAuthClient, Arrays.asList("read:users", "write:users", "read:admin", "write:admin"));
        OAuthConfigTestHelper.setAllowedCallbacks(oAuthClient, Collections.singletonList(VALID_REDIRECT_URI));

        return oAuthClient;
    }

    private static OAuthAuthorizationRequest prepareAuthorizationRequest() {

        return OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.CODE)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .redirectURI(SOURCE_O_AUTH_CLIENT.getAllowedCallbacks().get(0))
                .state("state-1")
                .scope(null)
                .build();
    }

    private static ExtendedUser prepareExtendedUser() {

        return ExtendedUser.builder()
                .id(1234L)
                .username("email@dev.local")
                .name("User 1")
                .role("ADMIN")
                .enabled(true)
                .authorities(AuthorityUtils.createAuthorityList("read:users", "write:users", "read:admin", "write:admin", "write:entries"))
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

    private static OAuthTokenRequest prepareOAuthTokenRequest() {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.AUTHORIZATION_CODE)
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID(O_AUTH_AUTHORIZATION_REQUEST.getClientID())
                .audience(TARGET_CLIENT_AUDIENCE)
                .scope(new LinkedList<>())
                .redirectURI(VALID_REDIRECT_URI)
                .build();
    }

    private static UserInfo prepareUserInfo() {

        return UserInfo.builder()
                .id(EXTENDED_USER.getId())
                .email(EXTENDED_USER.getUsername())
                .username(EXTENDED_USER.getName())
                .role(EXTENDED_USER.getRole())
                .build();
    }
}
