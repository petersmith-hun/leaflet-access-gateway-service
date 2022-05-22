package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link OngoingAuthorizationOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OngoingAuthorizationOAuthRequestVerifierTest {

    private static final String AUTHORIZATION_CODE = "auth-code-1";

    @Mock
    private OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @InjectMocks
    private OngoingAuthorizationOAuthRequestVerifier ongoingAuthorizationOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthTokenRequestContext context = prepareContext(true, true, true);

        // when
        ongoingAuthorizationOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @Test
    public void shouldVerifyRejectContextDueToMissingOngoingAuthorization() {

        // given
        OAuthTokenRequestContext context = prepareContextWithoutOngoingAuthorization();

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> ongoingAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Unknown authorization request"));
    }

    @Test
    public void shouldVerifyRejectContextForDifferentClientID() {

        // given
        OAuthTokenRequestContext context = prepareContext(false, true, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> ongoingAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Authorization request belongs to a different client."));

        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
    }

    @Test
    public void shouldVerifyRejectContextForDifferentRedirectURI() {

        // given
        OAuthTokenRequestContext context = prepareContext(true, false, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> ongoingAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Different redirect URI has been specified in the token request."));

        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
    }

    @Test
    public void shouldVerifyRejectContextDueToExpiredAuthorization() {

        // given
        OAuthTokenRequestContext context = prepareContext(true, true, false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> ongoingAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Authorization has already expired."));

        verify(ongoingAuthorizationRepository).deleteOngoingAuthorization(AUTHORIZATION_CODE);
    }

    @Test
    public void shouldForGrantTypeReturnAuthCode() {

        // when
        List<GrantType> result = ongoingAuthorizationOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(GrantType.AUTHORIZATION_CODE));
    }

    private OAuthTokenRequestContext prepareContext(boolean withValidClientID, boolean withValidRedirectURI, boolean withValidExpiration) {

        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization(withValidExpiration);

        return OAuthTokenRequestContext.builder()
                .ongoingAuthorization(Optional.of(ongoingAuthorization))
                .request(OAuthTokenRequest.builder()
                        .clientID(withValidClientID
                                ? ongoingAuthorization.getClientID()
                                : "different-client-id")
                        .redirectURI(withValidRedirectURI
                                ? ongoingAuthorization.getRedirectURI()
                                : "http://localhost:8888/invalid/callback")
                        .build())
                .build();
    }

    private OAuthTokenRequestContext prepareContextWithoutOngoingAuthorization() {

        return OAuthTokenRequestContext.builder()
                .ongoingAuthorization(Optional.empty())
                .build();
    }

    private OngoingAuthorization prepareOngoingAuthorization(boolean withValidExpiration) {

        return OngoingAuthorization.builder()
                .authorizationCode(AUTHORIZATION_CODE)
                .clientID("client-1")
                .redirectURI("http://localhost:9999/callback")
                .expiration(withValidExpiration
                        ? LocalDateTime.now().plusMinutes(1L)
                        : LocalDateTime.now().minusMinutes(1L))
                .build();
    }
}
