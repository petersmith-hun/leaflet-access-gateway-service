package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.TokenClaims;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.SOURCE_O_AUTH_CLIENT;
import static hu.psprog.leaflet.lags.core.domain.config.OAuthConfigTestHelper.TARGET_O_AUTH_CLIENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link PasswordGrantFlowProcessor}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordGrantFlowProcessorTest {

    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest(true);
    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST_WITHOUT_SCOPE = prepareValidOAuthTokenRequest(false);
    private static final UsernamePasswordAuthenticationToken EXPECTED_USER_AUTH_TOKEN = prepareExpectedUserAuthenticationToken();
    private static final UserDetails MOCK_USED_DETAILS = prepareMockUserDetails();

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier1;

    @Mock
    private OAuthRequestVerifier<OAuthTokenRequestContext> verifier2;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PasswordGrantFlowProcessor passwordGrantFlowProcessor;

    @Test
    public void shouldProcessTokenRequestGenerateClaimsWithSuccess() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.PASSWORD)).willReturn(List.of(verifier1, verifier2));
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);

        // when
        TokenClaims result = passwordGrantFlowProcessor.processTokenRequest(context);

        // then
        assertThat(result.getClaimsAsMap().size(), equalTo(6));
        assertThat(result.getClaimsAsMap(), equalTo(Map.of(
                "scope", "read:items write:item:self",
                "sub", "dummy-source-service-1|uid=1234",
                "usr", "user1",
                "rol", "USER",
                "name", "Some User",
                "uid", 1234L
        )));

        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.PASSWORD);
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
    }

    @Test
    public void shouldProcessTokenRequestGenerateClaimsWithDefaultScopeSet() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST_WITHOUT_SCOPE)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);

        // when
        TokenClaims result = passwordGrantFlowProcessor.processTokenRequest(context);

        // then
        assertThat(result.getClaimsAsMap().size(), equalTo(6));
        assertThat(result.getClaimsAsMap(), equalTo(Map.of(
                "scope", "default1 default2 default3",
                "sub", "dummy-source-service-1|uid=1234",
                "usr", "user1",
                "rol", "USER",
                "name", "Some User",
                "uid", 1234L
        )));
    }

    @Test
    public void shouldProcessTokenRequestThrowExceptionIfUserCannotBeAuthenticated() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.processTokenRequest(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Failed to authenticate user [user1]"));
    }

    @Test
    public void shouldProcessTokenRequestPassUpTheExceptionIfAVerifierFails() {

        // given
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        given(oAuthRequestVerifierRegistry.getTokenRequestVerifiers(GrantType.PASSWORD)).willReturn(List.of(verifier1, verifier2));
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        doThrow(OAuthAuthorizationException.class).when(verifier2).verify(context);

        // when
        assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.processTokenRequest(context));

        // then
        // exception expected

        verify(oAuthRequestVerifierRegistry).getTokenRequestVerifiers(GrantType.PASSWORD);
        verify(verifier1).verify(context);
        verify(verifier2).verify(context);
    }

    @ParameterizedTest
    @MethodSource("invalidUserDetailsDataProvider")
    public void shouldProcessTokenRequestThrowExceptionIfSecurityContextDoesNotContainProperPrincipal(UserDetails userDetails) {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        OAuthTokenRequestContext context = OAuthTokenRequestContext.builder()
                .request(VALID_O_AUTH_TOKEN_REQUEST)
                .sourceClient(SOURCE_O_AUTH_CLIENT)
                .build();

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.processTokenRequest(context));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Missing user details in security context for password grant flow"));
    }

    @Test
    public void shouldForGrantTypeReturnPassword() {

        // when
        GrantType result = passwordGrantFlowProcessor.forGrantType();

        // then
        assertThat(result, equalTo(GrantType.PASSWORD));
    }

    private static OAuthTokenRequest prepareValidOAuthTokenRequest(boolean withScope) {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.PASSWORD)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .audience(TARGET_O_AUTH_CLIENT.getAudience())
                .scope(withScope
                        ? Arrays.asList("read:items", "write:item:self")
                        : new ArrayList<>())
                .username("user1")
                .password("password1")
                .build();
    }

    private static UsernamePasswordAuthenticationToken prepareExpectedUserAuthenticationToken() {
        return new UsernamePasswordAuthenticationToken("user1", "password1");
    }

    private static UserDetails prepareMockUserDetails() {

        return ExtendedUser.builder()
                .id(1234L)
                .username("user1")
                .role("USER")
                .name("Some User")
                .authorities(AuthorityUtils.createAuthorityList("default1", "default2", "default3"))
                .build();
    }

    private static Stream<Arguments> invalidUserDetailsDataProvider() {

        return Stream.of(
                null,
                Arguments.of(new User("user1", "pass1", AuthorityUtils.createAuthorityList("USER")))
        );
    }
}
