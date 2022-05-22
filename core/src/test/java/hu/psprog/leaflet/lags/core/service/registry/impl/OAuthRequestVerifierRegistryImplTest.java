package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OAuthRequestVerifierRegistryImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OAuthRequestVerifierRegistryImplTest {

    @Mock
    private OAuthRequestVerifier<OAuthAuthorizationRequestContext> authorizationVerifier1;

    @Mock
    private OAuthRequestVerifier<OAuthAuthorizationRequestContext> authorizationVerifier2;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierClient;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierPassword;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierAuthCode;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierClientAndAuthCode;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierPasswordAndAuthCode;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierClientAndPassword;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> tokenVerifierAll;

    private OAuthRequestVerifierRegistryImpl oAuthRequestVerifierRegistry;

    @BeforeEach
    public void setup() {

        given(authorizationVerifier1.forGrantType()).willReturn(List.of(GrantType.AUTHORIZATION_CODE));
        given(authorizationVerifier2.forGrantType()).willReturn(List.of(GrantType.AUTHORIZATION_CODE));

        given(tokenVerifierClient.forGrantType()).willReturn(List.of(GrantType.CLIENT_CREDENTIALS));
        given(tokenVerifierPassword.forGrantType()).willReturn(List.of(GrantType.PASSWORD));
        given(tokenVerifierAuthCode.forGrantType()).willReturn(List.of(GrantType.AUTHORIZATION_CODE));
        given(tokenVerifierClientAndAuthCode.forGrantType()).willReturn(List.of(GrantType.CLIENT_CREDENTIALS, GrantType.AUTHORIZATION_CODE));
        given(tokenVerifierPasswordAndAuthCode.forGrantType()).willReturn(List.of(GrantType.PASSWORD, GrantType.AUTHORIZATION_CODE));
        given(tokenVerifierClientAndPassword.forGrantType()).willReturn(List.of(GrantType.CLIENT_CREDENTIALS, GrantType.PASSWORD));
        given(tokenVerifierAll.forGrantType()).willReturn(List.of(GrantType.CLIENT_CREDENTIALS, GrantType.PASSWORD, GrantType.AUTHORIZATION_CODE));

        oAuthRequestVerifierRegistry = new OAuthRequestVerifierRegistryImpl(
                Arrays.asList(authorizationVerifier1, authorizationVerifier2),
                Arrays.asList(tokenVerifierClient, tokenVerifierPassword, tokenVerifierAuthCode, tokenVerifierClientAndAuthCode,
                        tokenVerifierPasswordAndAuthCode, tokenVerifierClientAndPassword, tokenVerifierAll)
        );
    }

    @Test
    public void shouldGetAuthorizationRequestVerifiersReturnRelevantVerifiers() {

        // when
        List<OAuthRequestVerifier<OAuthAuthorizationRequestContext>> result = oAuthRequestVerifierRegistry.getAuthorizationRequestVerifiers();

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, hasItems(
                authorizationVerifier1,
                authorizationVerifier2
        ));
    }

    @Test
    public void shouldGetTokenRequestVerifiersReturnRelevantVerifiersForAuthCodeGrant() {

        // when
        List<OAuthRequestVerifier<OAuthTokenRequestContext>> result = oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.AUTHORIZATION_CODE);

        // then
        assertThat(result.size(), equalTo(4));
        assertThat(result, hasItems(
                tokenVerifierAuthCode,
                tokenVerifierClientAndAuthCode,
                tokenVerifierPasswordAndAuthCode,
                tokenVerifierAll
        ));
    }

    @Test
    public void shouldGetTokenRequestVerifiersReturnRelevantVerifiersForClientCredentialsGrant() {

        // when
        List<OAuthRequestVerifier<OAuthTokenRequestContext>> result = oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.CLIENT_CREDENTIALS);

        // then
        assertThat(result.size(), equalTo(4));
        assertThat(result, hasItems(
                tokenVerifierClient,
                tokenVerifierClientAndAuthCode,
                tokenVerifierClientAndPassword,
                tokenVerifierAll
        ));
    }

    @Test
    public void shouldGetTokenRequestVerifiersReturnRelevantVerifiersForPasswordGrant() {

        // when
        List<OAuthRequestVerifier<OAuthTokenRequestContext>> result = oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.PASSWORD);

        // then
        assertThat(result.size(), equalTo(4));
        assertThat(result, hasItems(
                tokenVerifierPassword,
                tokenVerifierPasswordAndAuthCode,
                tokenVerifierClientAndPassword,
                tokenVerifierAll
        ));
    }
}
