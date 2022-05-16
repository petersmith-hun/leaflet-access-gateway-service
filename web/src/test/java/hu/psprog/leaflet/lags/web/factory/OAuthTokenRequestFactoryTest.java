package hu.psprog.leaflet.lags.web.factory;

import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuthTokenRequestFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthTokenRequestFactoryTest {

    private static final Map<String, String> REQUEST_PARAMETERS = Map.of(
            "client_id", "client1",
            "grant_type", "password",
            "username", "user1",
            "password", "pass1",
            "audience", "aud1",
            "scope", "scope1 scope2"
    );

    private static final Map<String, String> REQUEST_PARAMETERS_WITHOUT_SCOPE = Map.of(
            "client_id", "client1",
            "grant_type", "password",
            "username", "user1",
            "password", "pass1",
            "audience", "aud1"
    );

    private static final Map<String, String> REQUEST_PARAMETERS_WITHOUT_CLIENT_ID = Map.of(
            "grant_type", "password",
            "username", "user1",
            "password", "pass1",
            "audience", "aud1",
            "scope", "scope1 scope2"
    );

    private static final Map<String, String> REQUEST_PARAMETERS_WITH_INVALID_GRANT_TYPE = Map.of(
            "client_id", "client1",
            "grant_type", "some-invalid-grant-type",
            "username", "user1",
            "password", "pass1",
            "audience", "aud1",
            "scope", "scope1 scope2"
    );

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private OAuthTokenRequestFactory oAuthTokenRequestFactory;

    @Test
    public void shouldCreateTokenRequestWithSpecifiedClientID() {

        // given
        given(userDetails.getUsername()).willReturn("client1");

        // when
        OAuthTokenRequest result = oAuthTokenRequestFactory.createTokenRequest(REQUEST_PARAMETERS, userDetails);

        // then
        verifyOAuthTokenRequest(result, true);
    }

    @Test
    public void shouldCreateTokenRequestWithSpecifiedClientIDAndMissingScope() {

        // given
        given(userDetails.getUsername()).willReturn("client1");

        // when
        OAuthTokenRequest result = oAuthTokenRequestFactory.createTokenRequest(REQUEST_PARAMETERS_WITHOUT_SCOPE, userDetails);

        // then
        verifyOAuthTokenRequest(result, false);
    }

    @Test
    public void shouldCreateTokenRequestThrowExceptionForInvalidGrantType() {

        // given
        given(userDetails.getUsername()).willReturn("client1");

        // when
        assertThrows(OAuthAuthorizationException.class,
                () -> oAuthTokenRequestFactory.createTokenRequest(REQUEST_PARAMETERS_WITH_INVALID_GRANT_TYPE, userDetails));

        // then
        // exception expected
    }

    @Test
    public void shouldCreateTokenRequestWithCopiedClientIDFromAuthentication() {

        // given
        given(userDetails.getUsername()).willReturn("client1");

        // when
        OAuthTokenRequest result = oAuthTokenRequestFactory.createTokenRequest(new HashMap<>(REQUEST_PARAMETERS_WITHOUT_CLIENT_ID), userDetails);

        // then
        verifyOAuthTokenRequest(result, true);
    }

    @Test
    public void shouldCreateTokenRequestThrowExceptionForSpecifiedAndDifferentClientIDInAuthentication() {

        // given
        given(userDetails.getUsername()).willReturn("client2");

        // when
        assertThrows(OAuthAuthorizationException.class,
                () -> oAuthTokenRequestFactory.createTokenRequest(REQUEST_PARAMETERS, userDetails));

        // then
        // exception expected
    }

    private void verifyOAuthTokenRequest(OAuthTokenRequest result, boolean withScope) {

        assertThat(result.getClientID(), equalTo("client1"));
        assertThat(result.getGrantType(), equalTo(GrantType.PASSWORD));
        assertThat(result.getAudience(), equalTo("aud1"));
        assertThat(result.getUsername(), equalTo("user1"));
        assertThat(result.getPassword(), equalTo("pass1"));
        assertThat(result.getScope(), equalTo(withScope
                ? Arrays.asList("scope1", "scope2")
                : Collections.emptyList()));
    }
}
