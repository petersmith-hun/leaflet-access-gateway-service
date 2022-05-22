package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.registry.OAuthClientRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.INVALID_TARGET_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.SOURCE_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.TARGET_O_AUTH_CLIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuthRequestContextFactoryImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthRequestContextFactoryImplTest {

    private static final String SOURCE_CLIENT_ID = "dummy-source-service-1";
    private static final String TARGET_SERVICE_AUDIENCE = "target-service-audience";
    private static final String AUTHORIZATION_CODE = "auth-code-1";
    private static final ExtendedUser EXTENDED_USER = ExtendedUser.builder().username("user1").build();
    private static final OngoingAuthorization ONGOING_AUTHORIZATION = prepareOngoingAuthorization();

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @InjectMocks
    private OAuthRequestContextFactoryImpl oAuthRequestContextFactory;

    @Test
    public void shouldCreateContextPrepareAnAuthorizationRequestContext() {

        // given
        prepareAuthenticatedUser();
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.of(SOURCE_O_AUTH_CLIENT));

        // when
        OAuthAuthorizationRequestContext result = oAuthRequestContextFactory.createContext(request);

        // then
        assertThat(result.getRequest(), equalTo(request));
        assertThat(result.getAuthenticatedUser(), equalTo(EXTENDED_USER));
        assertThat(result.getSourceClient(), equalTo(SOURCE_O_AUTH_CLIENT));

        clearAuthenticatedUser();
    }

    @Test
    public void shouldCreateContextThrowExceptionWhilePreparingAuthorizationContextIfSourceClientIsUnknown() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthRequestContextFactory.createContext(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth client by ID [dummy-source-service-1] is not registered"));
    }

    @Test
    public void shouldCreateContextPrepareATokenRequestContext() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.of(SOURCE_O_AUTH_CLIENT));
        given(oAuthClientRegistry.getClientByAudience(TARGET_SERVICE_AUDIENCE)).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));
        given(ongoingAuthorizationRepository.getOngoingAuthorizationByCode(AUTHORIZATION_CODE)).willReturn(Optional.of(ONGOING_AUTHORIZATION));

        // when
        OAuthTokenRequestContext result = oAuthRequestContextFactory.createContext(request);

        // then
        assertThat(result.getRequest(), equalTo(request));
        assertThat(result.getSourceClient(), equalTo(SOURCE_O_AUTH_CLIENT));
        assertThat(result.getTargetClient(), equalTo(TARGET_O_AUTH_CLIENT));
        assertThat(result.getOngoingAuthorization().isPresent(), is(true));
        assertThat(result.getOngoingAuthorization().get(), equalTo(ONGOING_AUTHORIZATION));
        assertThat(result.getRequiredOngoingAuthorization(), equalTo(ONGOING_AUTHORIZATION));
        assertThat(result.getRelation(), equalTo(TARGET_O_AUTH_CLIENT.getAllowedClients().get(0)));
    }

    @Test
    public void shouldCreateContextThrowExceptionWhilePreparingTokenContextIfSourceClientIsUnknown() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthRequestContextFactory.createContext(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("OAuth client by ID [dummy-source-service-1] is not registered"));
    }

    @Test
    public void shouldCreateContextThrowExceptionWhilePreparingTokenContextIfTargetClientIsUnknown() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.of(SOURCE_O_AUTH_CLIENT));
        given(oAuthClientRegistry.getClientByAudience(TARGET_SERVICE_AUDIENCE)).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthRequestContextFactory.createContext(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Requested access for non-registered OAuth client [target-service-audience]"));
    }

    @Test
    public void shouldCreateContextThrowExceptionWhilePreparingTokenContextIfRelationIsNotRegistered() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest();

        given(oAuthClientRegistry.getClientByClientID(SOURCE_CLIENT_ID)).willReturn(Optional.of(SOURCE_O_AUTH_CLIENT));
        given(oAuthClientRegistry.getClientByAudience(TARGET_SERVICE_AUDIENCE)).willReturn(Optional.of(INVALID_TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> oAuthRequestContextFactory.createContext(request));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Target client [target-service-1] does not allow access for source client [source-service-1]"));
    }

    private void prepareAuthenticatedUser() {

        SecurityContextHolder
                .getContext()
                .setAuthentication(new TestingAuthenticationToken(EXTENDED_USER, null));
    }

    private void clearAuthenticatedUser() {
        SecurityContextHolder.clearContext();
    }

    private OAuthAuthorizationRequest prepareOAuthAuthorizationRequest() {

        return OAuthAuthorizationRequest.builder()
                .clientID(SOURCE_CLIENT_ID)
                .build();
    }

    private OAuthTokenRequest prepareOAuthTokenRequest() {

        return OAuthTokenRequest.builder()
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID(SOURCE_CLIENT_ID)
                .audience(TARGET_SERVICE_AUDIENCE)
                .build();
    }

    private static OngoingAuthorization prepareOngoingAuthorization() {

        return OngoingAuthorization.builder()
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID(SOURCE_CLIENT_ID)
                .build();
    }
}
