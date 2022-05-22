package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ClientCredentialsTokenOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ClientCredentialsTokenOAuthRequestVerifierTest {

    @InjectMocks
    private ClientCredentialsTokenOAuthRequestVerifier clientCredentialsTokenOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthTokenRequestContext context = prepareContext(List.of("read:admin"));

        // when
        clientCredentialsTokenOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @Test
    public void shouldVerifyRejectContextForNonDefinedScope() {

        // given
        OAuthTokenRequestContext context = prepareContext(null);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Value for required authorization parameter [scope] is missing"));
    }

    @Test
    public void shouldVerifyRejectContextForEmptyScope() {

        // given
        OAuthTokenRequestContext context = prepareContext(Collections.emptyList());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> clientCredentialsTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Value for required authorization parameter [scope] is missing"));
    }

    @Test
    public void shouldForGrantTypeReturnClientCredentials() {

        // when
        List<GrantType> result = clientCredentialsTokenOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(GrantType.CLIENT_CREDENTIALS));
    }

    private OAuthTokenRequestContext prepareContext(List<String> scope) {

        return OAuthTokenRequestContext.builder()
                .request(OAuthTokenRequest.builder()
                        .scope(scope)
                        .build())
                .build();
    }
}
