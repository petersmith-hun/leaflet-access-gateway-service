package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ResourceOwnerTokenOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class ResourceOwnerTokenOAuthRequestVerifierTest {

    @InjectMocks
    private ResourceOwnerTokenOAuthRequestVerifier resourceOwnerTokenOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthTokenRequestContext context = prepareContext("user1", "pass1");

        // when
        resourceOwnerTokenOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', nullValues = "null", value = {
            "'';pass;username",
            "null;pass;username",
            "user;'';password",
            "user;null;password"
    })
    public void shouldVerifyRejectContextForMissingField(String username, String password, String expectedMissingFieldName) {

        // given
        OAuthTokenRequestContext context = prepareContext(username, password);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> resourceOwnerTokenOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", expectedMissingFieldName)));
    }

    @Test
    public void shouldForGrantTypeReturnPasswordGrant() {

        // when
        List<GrantType> result = resourceOwnerTokenOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(GrantType.PASSWORD));
    }

    private OAuthTokenRequestContext prepareContext(String username, String password) {

        return OAuthTokenRequestContext.builder()
                .request(OAuthTokenRequest.builder()
                        .username(username)
                        .password(password)
                        .build())
                .build();
    }
}
