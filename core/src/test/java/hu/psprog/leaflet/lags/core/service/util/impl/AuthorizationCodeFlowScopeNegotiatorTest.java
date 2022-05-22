package hu.psprog.leaflet.lags.core.service.util.impl;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link AuthorizationCodeFlowScopeNegotiator}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationCodeFlowScopeNegotiatorTest {

    @InjectMocks
    private AuthorizationCodeFlowScopeNegotiator authorizationCodeFlowScopeNegotiator;

    @Test
    public void shouldGetScopeForAuthorizationContextVerifyAndReturnUserScopeSet() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest(null);
        OAuthClient sourceClient = prepareOAuthClient();
        ExtendedUser extendedUser = prepareExtendedUser(true);
        OAuthAuthorizationRequestContext context = prepareAuthorizationContext(request, sourceClient, extendedUser);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(6));
        assertThat(result, hasItems("read:users:own", "write:users:own", "read:comments:own", "write:comments:own", "write:entries", "write:categories"));
    }

    @Test
    public void shouldGetScopeForAuthorizationContextVerifyAndReturnDefinedScopeSet() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest("write:entries write:categories");
        OAuthClient sourceClient = prepareOAuthClient();
        ExtendedUser extendedUser = prepareExtendedUser(true);
        OAuthAuthorizationRequestContext context = prepareAuthorizationContext(request, sourceClient, extendedUser);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, hasItems("write:entries", "write:categories"));
    }

    @Test
    public void shouldGetScopeForAuthorizationContextThrowExceptionOnInvalidUser() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest(null);
        OAuthClient sourceClient = prepareOAuthClient();
        ExtendedUser extendedUser = prepareExtendedUser(false);
        OAuthAuthorizationRequestContext context = prepareAuthorizationContext(request, sourceClient, extendedUser);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeFlowScopeNegotiator.getScope(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client requires broader authorities than what the user has."));
    }

    @Test
    public void shouldGetScopeForAuthorizationContextThrowExceptionOnInvalidUserWithDefinedScope() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest("write:comments:own write:entries");
        OAuthClient sourceClient = prepareOAuthClient();
        ExtendedUser extendedUser = prepareExtendedUser(false);
        OAuthAuthorizationRequestContext context = prepareAuthorizationContext(request, sourceClient, extendedUser);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeFlowScopeNegotiator.getScope(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client requires broader authorities than what the user has."));
    }

    @Test
    public void shouldGetScopeForAuthorizationContextThrowExceptionOnInvalidDefinedScope() {

        // given
        OAuthAuthorizationRequest request = prepareOAuthAuthorizationRequest("read:admin write:admin");
        OAuthClient sourceClient = prepareOAuthClient();
        ExtendedUser extendedUser = prepareExtendedUser(true);
        OAuthAuthorizationRequestContext context = prepareAuthorizationContext(request, sourceClient, extendedUser);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeFlowScopeNegotiator.getScope(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Client requires broader authorities than what the user has."));
    }

    @Test
    public void shouldGetScopeForTokenContextReturnAuthorizedScope() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(false);
        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization("read:users", "write:users");
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, ongoingAuthorization, relation);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, hasItems(
                "read:users",
                "write:users"
        ));
    }

    @Test
    public void shouldGetScopeForTokenContextReturnRelationScope() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(false);
        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization("read:users", "write:users", "read:comments", "write:comments", "read:admin", "write:admin");
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, ongoingAuthorization, relation);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(4));
        assertThat(result, hasItems(
                "read:users",
                "write:users",
                "read:comments",
                "write:comments"
        ));
    }

    @Test
    public void shouldGetScopeForTokenContextReturnRelationScopeForBroaderAuthorizedScopeWithMissingScopes() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(false);
        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization("read:users", "write:users", "read:comments", "read:admin", "write:admin");
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, ongoingAuthorization, relation);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(3));
        assertThat(result, hasItems(
                "read:users",
                "write:users",
                "read:comments"
        ));
    }

    @Test
    public void shouldGetScopeForTokenContextReturnRelationScopeForBroaderAuthorizedScope() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(false);
        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization("read:users", "read:comments", "read:admin", "write:admin");
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, ongoingAuthorization, relation);

        // when
        List<String> result = authorizationCodeFlowScopeNegotiator.getScope(context);

        // then
        assertThat(result.size(), equalTo(2));
        assertThat(result, hasItems(
                "read:users",
                "read:comments"
        ));
    }

    @Test
    public void shouldGetScopeForTokenContextThrowExceptionOnDefinedScopeInTheRequest() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(true);
        OngoingAuthorization ongoingAuthorization = prepareOngoingAuthorization("read:users", "write:users");
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, ongoingAuthorization, relation);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeFlowScopeNegotiator.getScope(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Token request should not specify scope on Authorization Code flow."));
    }

    @Test
    public void shouldGetScopeForTokenContextThrowExceptionOnNonExistingOngoingAuthorization() {

        // given
        OAuthTokenRequest request = prepareOAuthTokenRequest(false);
        OAuthClientAllowRelation relation = prepareRelation();
        OAuthTokenRequestContext context = prepareTokenContext(request, null, relation);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> authorizationCodeFlowScopeNegotiator.getScope(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Missing ongoing authorization"));
    }

    private OAuthAuthorizationRequestContext prepareAuthorizationContext(OAuthAuthorizationRequest request, OAuthClient sourceClient, ExtendedUser extendedUser) {

        return OAuthAuthorizationRequestContext.builder()
                .request(request)
                .sourceClient(sourceClient)
                .authenticatedUser(extendedUser)
                .build();
    }

    private OAuthTokenRequestContext prepareTokenContext(OAuthTokenRequest request, OngoingAuthorization ongoingAuthorization, OAuthClientAllowRelation relation) {

        return OAuthTokenRequestContext.builder()
                .request(request)
                .ongoingAuthorization(Optional.ofNullable(ongoingAuthorization))
                .relation(relation)
                .build();
    }

    private OAuthAuthorizationRequest prepareOAuthAuthorizationRequest(String scope) {

        return OAuthAuthorizationRequest.builder()
                .scope(scope)
                .build();
    }

    private ExtendedUser prepareExtendedUser(boolean elevatedRole) {

        return ExtendedUser.builder()
                .authorities(prepareAuthorities(elevatedRole))
                .build();
    }

    private List<GrantedAuthority> prepareAuthorities(boolean elevatedRole) {

        return elevatedRole
                ? AuthorityUtils.createAuthorityList("read:users:own", "write:users:own", "read:comments:own", "write:comments:own", "write:entries", "write:categories")
                : AuthorityUtils.createAuthorityList("read:users:own", "write:users:own", "read:comments:own", "write:comments:own");
    }

    private OAuthClient prepareOAuthClient() {

        OAuthClient client = OAuthConfigTestHelper.prepareOAuthClient("client-1", ApplicationType.UI, "client-id-1", "secret1", "audience-1");
        OAuthConfigTestHelper.setRequiredScopes(client, Arrays.asList("write:categories", "write:entries"));

        return client;
    }

    private OAuthTokenRequest prepareOAuthTokenRequest(boolean withScope) {

        return OAuthTokenRequest.builder()
                .scope(withScope
                        ? List.of("some:scope")
                        : Collections.emptyList())
                .build();
    }

    private OngoingAuthorization prepareOngoingAuthorization(String... scope) {

        return OngoingAuthorization.builder()
                .scope(Arrays.asList(scope))
                .build();
    }

    private OAuthClientAllowRelation prepareRelation() {
        return OAuthConfigTestHelper.prepareRelation("client2", Arrays.asList("read:users", "write:users", "read:comments", "write:comments"));
    }
}
