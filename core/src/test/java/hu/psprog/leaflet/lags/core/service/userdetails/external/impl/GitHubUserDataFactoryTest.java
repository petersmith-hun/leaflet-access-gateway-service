package hu.psprog.leaflet.lags.core.service.userdetails.external.impl;

import hu.psprog.leaflet.bridge.client.BridgeClient;
import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.bridge.client.exception.ForbiddenOperationException;
import hu.psprog.leaflet.bridge.client.request.RESTRequest;
import hu.psprog.leaflet.bridge.client.request.RequestMethod;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.internal.GitHubEmailItem;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import jakarta.ws.rs.core.GenericType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

/**
 * Unit tests for {@link GitHubUserDataFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class GitHubUserDataFactoryTest {

    private static final int USER_ID = 1122;
    private static final String USERNAME = "External User";
    private static final String EMAIL = "externaluser1-2@dev.local";
    private static final String REGISTRATION_ID = "github";

    private static final OAuth2UserRequest OAUTH_USER_REQUEST = prepareUserRequest();
    private static final OAuth2User OAUTH_USER = prepareOAuth2User();
    private static final ExternalUserDefinition<Long> EXPECTED_EXTERNAL_USER_DEFINITION = prepareExternalUserDefinition();

    private static final GenericType<List<GitHubEmailItem>> GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE = new GenericType<>() {};
    private static final List<GitHubEmailItem> GITHUB_EMAIL_ITEMS = prepareGitHubEmailResponse(true);
    private static final List<GitHubEmailItem> GITHUB_EMAIL_ITEMS_WITHOUT_PRIMARY = prepareGitHubEmailResponse(false);

    @Mock
    private BridgeClient bridgeClient;

    @Captor
    private ArgumentCaptor<RESTRequest> restRequestCaptor;

    @InjectMocks
    private GitHubUserDataFactory gitHubUserDataFactory;

    @Test
    public void shouldCreateUserDefinitionProcessRequestSuccessfully() throws CommunicationFailureException {

        // given
        given(bridgeClient.call(restRequestCaptor.capture(), eq(GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE))).willReturn(GITHUB_EMAIL_ITEMS);

        // when
        ExternalUserDefinition<Long> result = gitHubUserDataFactory.createUserDefinition(OAUTH_USER_REQUEST, OAUTH_USER);

        // then
        assertThat(result, equalTo(EXPECTED_EXTERNAL_USER_DEFINITION));
        assertRESTRequest(restRequestCaptor.getValue());
    }

    @Test
    public void shouldCreateUserDefinitionFailForExternalUserNotHavingPrimaryEmailAddress() throws CommunicationFailureException {

        // given
        given(bridgeClient.call(any(), eq(GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE))).willReturn(GITHUB_EMAIL_ITEMS_WITHOUT_PRIMARY);

        // when
        assertThrows(ExternalAuthenticationException.class, () -> gitHubUserDataFactory.createUserDefinition(OAUTH_USER_REQUEST, OAUTH_USER));

        // then
        // exception expected
    }

    @Test
    public void shouldCreateUserDefinitionFailWhenContactGitHubAPIFails() throws CommunicationFailureException {

        // given
        doThrow(ForbiddenOperationException.class).when(bridgeClient).call(any(), eq(GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE));

        // when
        assertThrows(ExternalAuthenticationException.class, () -> gitHubUserDataFactory.createUserDefinition(OAUTH_USER_REQUEST, OAUTH_USER));

        // then
        // exception expected
    }

    @Test
    public void shouldForProviderReturnGitHub() {

        // when
        String result = gitHubUserDataFactory.forProvider();

        // then
        assertThat(result, equalTo(REGISTRATION_ID));
    }

    private void assertRESTRequest(RESTRequest restRequest) {

        assertThat(restRequest.getMethod(), equalTo(RequestMethod.GET));
        assertThat(restRequest.getPath().getURI(), equalTo("/user/emails"));
        assertThat(restRequest.getHeaderParameters().size(), equalTo(1));
        assertThat(restRequest.getHeaderParameters().get("Authorization"), equalTo("Bearer token1"));

        assertThat(restRequest.getRequestBody(), nullValue());
        assertThat(restRequest.getPathParameters().isEmpty(), is(true));
        assertThat(restRequest.getRequestParameters().isEmpty(), is(true));
        assertThat(restRequest.isAuthenticationRequired(), is(false));
    }

    private static OAuth2UserRequest prepareUserRequest() {

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId(REGISTRATION_ID)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId("client-id-1")
                .tokenUri("http://localhost")
                .build();

        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token1",
                Instant.now(), Instant.now().plusSeconds(10L));

        return new OAuth2UserRequest(clientRegistration, oAuth2AccessToken, Collections.emptyMap());
    }

    private static OAuth2User prepareOAuth2User() {

        return new DefaultOAuth2User(Collections.emptyList(), Map.of(
                "id", USER_ID,
                "name", USERNAME
        ), "name");
    }

    private static List<GitHubEmailItem> prepareGitHubEmailResponse(boolean includePrimary) {

        return List.of(
                prepareGitHubEmailItem("externaluser1-1@dev.local", false),
                prepareGitHubEmailItem(EMAIL, includePrimary),
                prepareGitHubEmailItem("externaluser1-3@dev.local", false)
        );
    }

    private static ExternalUserDefinition<Long> prepareExternalUserDefinition() {

        return ExternalUserDefinition.<Long>builder()
                .accountType(AccountType.GITHUB)
                .userID((long) USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .build();
    }

    private static GitHubEmailItem prepareGitHubEmailItem(String email, boolean primary) {

        GitHubEmailItem emailItem = new GitHubEmailItem();
        emailItem.setEmail(email);
        emailItem.setPrimary(primary);

        return emailItem;
    }
}
