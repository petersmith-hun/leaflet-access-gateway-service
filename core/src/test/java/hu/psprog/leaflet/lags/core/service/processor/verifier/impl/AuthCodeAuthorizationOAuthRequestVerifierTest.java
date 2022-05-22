package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AuthCodeAuthorizationOAuthRequestVerifier}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthCodeAuthorizationOAuthRequestVerifierTest {

    @InjectMocks
    private AuthCodeAuthorizationOAuthRequestVerifier authCodeAuthorizationOAuthRequestVerifier;

    @Test
    public void shouldVerifyAcceptContext() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(true, true, true, true, true);

        // when
        authCodeAuthorizationOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @Test
    public void shouldVerifyAcceptContextWithoutRequestedScope() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(true, true, true, false, true);

        // when
        authCodeAuthorizationOAuthRequestVerifier.verify(context);

        // then
        // silent fall-through expected
    }

    @Test
    public void shouldVerifyRejectContextForInvalidApplicationType() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(false, true, true, true, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authCodeAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client application is not permitted to use authorization code flow."));
    }

    @Test
    public void shouldVerifyRejectContextForInvalidCallback() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(true, false, true, true, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authCodeAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Specified redirection URI [http://localhost:8888/invalid/callback] is not registered"));
    }

    @Test
    public void shouldVerifyRejectContextForInvalidResponseType() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(true, true, false, true, true);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authCodeAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Authorization response type must be [code]"));
    }

    @Test
    public void shouldVerifyRejectContextForInvalidRequestedScope() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext(true, true, true, true, false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authCodeAuthorizationOAuthRequestVerifier.verify(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Requested scope is broader than the user's authority range."));
    }

    @Test
    public void shouldForGrantTypeReturnAuthCode() {

        // when
        List<GrantType> result = authCodeAuthorizationOAuthRequestVerifier.forGrantType();

        // then
        assertThat(result.size(), equalTo(1));
        assertThat(result.get(0), equalTo(GrantType.AUTHORIZATION_CODE));
    }

    private OAuthAuthorizationRequestContext prepareContext(boolean withValidApplicationType, boolean withValidCallback,
                                                            boolean withValidResponseType, boolean withSpecifiedScope, boolean withValidScope) {

        OAuthClient oAuthClient = OAuthConfigTestHelper.prepareOAuthClient("client1",
                withValidApplicationType ? ApplicationType.UI : ApplicationType.SERVICE,
                "client-id-1", "client-secret-1", "audience-1");

        OAuthConfigTestHelper.setAllowedCallbacks(oAuthClient, List.of("http://localhost:9999/callback"));

        return OAuthAuthorizationRequestContext.builder()
                .sourceClient(oAuthClient)
                .authenticatedUser(prepareExtendedUser())
                .request(OAuthAuthorizationRequest.builder()
                        .responseType(withValidResponseType
                                ? AuthorizationResponseType.CODE
                                : null)
                        .redirectURI(withValidCallback
                                ? oAuthClient.getAllowedCallbacks().get(0)
                                : "http://localhost:8888/invalid/callback")
                        .scope(withSpecifiedScope
                                ? (withValidScope
                                    ? "read:users:own write:users:own"
                                    : "write:admin read:admin")
                                : null)
                        .build())
                .build();
    }

    private ExtendedUser prepareExtendedUser() {

        return ExtendedUser.builder()
                .authorities(AuthorityUtils.createAuthorityList("read:users:own", "write:users:own", "read:comments:own", "write:comments:own"))
                .build();
    }
}
