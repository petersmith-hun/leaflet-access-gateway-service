package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link PasswordGrantFlowProcessor}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class PasswordGrantFlowProcessorTest {

    private static final OAuthClient SOURCE_O_AUTH_CLIENT = prepareSourceOAuthClient();
    private static final OAuthClient TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(true);
    private static final OAuthClient INVALID_TARGET_O_AUTH_CLIENT = prepareTargetOAuthClient(false);
    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST = prepareValidOAuthTokenRequest(true);
    private static final OAuthTokenRequest VALID_O_AUTH_TOKEN_REQUEST_WITHOUT_SCOPE = prepareValidOAuthTokenRequest(false);
    private static final UsernamePasswordAuthenticationToken EXPECTED_USER_AUTH_TOKEN = prepareExpectedUserAuthenticationToken();
    private static final UserDetails MOCK_USED_DETAILS = prepareMockUserDetails();

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private OAuthClientRegistry oAuthClientRegistry;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PasswordGrantFlowProcessor passwordGrantFlowProcessor;

    @Test
    public void shouldVerifyRequestGenerateClaimsWithSuccess() {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Map<String, Object> result = passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT);

        // then
        assertThat(result.size(), equalTo(6));
        assertThat(result, equalTo(Map.of(
                "scope", "read:items write:item:self",
                "sub", "dummy-source-service-1|uid=1234",
                "usr", "user1",
                "rol", "USER",
                "name", "Some User",
                "uid", 1234L
        )));
    }

    @Test
    public void shouldVerifyRequestGenerateClaimsWithDefaultScopeSet() {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Map<String, Object> result = passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST_WITHOUT_SCOPE, SOURCE_O_AUTH_CLIENT);

        // then
        assertThat(result.size(), equalTo(6));
        assertThat(result, equalTo(Map.of(
                "scope", "default1 default2 default3",
                "sub", "dummy-source-service-1|uid=1234",
                "usr", "user1",
                "rol", "USER",
                "name", "Some User",
                "uid", 1234L
        )));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfUserCannotBeAuthenticated() {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(false);

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Failed to authenticate user [user1]"));
    }

    @ParameterizedTest
    @MethodSource("missingFieldDataProvider")
    public void shouldVerifyRequestThrowExceptionIfMandatoryFieldsAreMissing(String missingFieldName, OAuthTokenRequest oAuthTokenRequest) {

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(oAuthTokenRequest, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Value for required authorization parameter [%s] is missing", missingFieldName)));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfTargetClientIsNotRegistered() {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.empty());

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Requested access for non-registered OAuth client [target-service-audience]"));
    }

    @Test
    public void shouldVerifyRequestThrowExceptionIfAccessingTargetIsNotAllowedToSourceClient() {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(INVALID_TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo("Target client [target-service-1] does not allow access for source client [source-service-1]"));
    }

    @ParameterizedTest
    @MethodSource("requestWithInvalidScopeDataProvider")
    public void shouldVerifyRequestThrowExceptionIfSourceRequestedNotAllowedScope(OAuthTokenRequest oAuthTokenRequest) {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(MOCK_USED_DETAILS);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(oAuthTokenRequest, SOURCE_O_AUTH_CLIENT));

        // then
        // exception expected
        assertThat(result.getMessage(), equalTo(String.format("Target client [target-service-1] does not allow the requested scope [%s] for source client [source-service-1]",
                oAuthTokenRequest.getScope())));
    }

    @ParameterizedTest
    @MethodSource("invalidUserDetailsDataProvider")
    public void shouldVerifyRequestThrowExceptionIfSecurityContextDoesNotContainProperPrincipal(UserDetails userDetails) {

        // given
        given(authenticationProvider.authenticate(EXPECTED_USER_AUTH_TOKEN)).willReturn(authentication);
        given(authentication.isAuthenticated()).willReturn(true);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(oAuthClientRegistry.getClientByAudience(TARGET_O_AUTH_CLIENT.getAudience())).willReturn(Optional.of(TARGET_O_AUTH_CLIENT));

        // when
        Throwable result = assertThrows(OAuthAuthorizationException.class, () -> passwordGrantFlowProcessor.verifyRequest(VALID_O_AUTH_TOKEN_REQUEST, SOURCE_O_AUTH_CLIENT));

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

    private static OAuthClient prepareSourceOAuthClient() {

        return new OAuthClient(
                "source-service-1",
                "dummy-source-service-1",
                "secret1",
                "source-service-audience",
                Collections.emptyList(),
                Collections.emptyList());
    }

    private static OAuthClient prepareTargetOAuthClient(boolean validRelation) {

        OAuthClientAllowRelation relation = new OAuthClientAllowRelation(
                validRelation ? "source-service-1" : "source-service-2",
                Arrays.asList("read:items", "write:item:self", "default1", "default2", "default3"));

        return new OAuthClient(
                "target-service-1",
                "dummy-target-service-1",
                "secret2",
                "target-service-audience",
                Arrays.asList("read:items", "write:item:all", "write:item:self", "admin:item", "default1", "default2", "default3"),
                Collections.singletonList(relation));
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

    private static OAuthTokenRequest prepareOAuthTokenRequest(String... scope) {

        return OAuthTokenRequest.builder()
                .grantType(GrantType.PASSWORD)
                .clientID(SOURCE_O_AUTH_CLIENT.getClientId())
                .audience(TARGET_O_AUTH_CLIENT.getAudience())
                .scope(Arrays.asList(scope))
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

    private static Stream<Arguments> missingFieldDataProvider() {

        return Stream.of(
                Arguments.of("client_id", OAuthTokenRequest.builder().audience("audience1").username("user1").password("pass1").build()),
                Arguments.of("audience", OAuthTokenRequest.builder().clientID("client1").username("user1").password("pass1").build()),
                Arguments.of("username", OAuthTokenRequest.builder().clientID("client1").audience("audience1").password("pass1").build()),
                Arguments.of("password", OAuthTokenRequest.builder().clientID("client1").audience("audience1").username("user1").build())
        );
    }

    private static Stream<Arguments> requestWithInvalidScopeDataProvider() {

        return Stream.of(
                Arguments.of(prepareOAuthTokenRequest("admin:item")),
                Arguments.of(prepareOAuthTokenRequest("admin:item", "read:items", "write:item:self")),
                Arguments.of(prepareOAuthTokenRequest("some:non:existing", "read:items"))
        );
    }

    private static Stream<Arguments> invalidUserDetailsDataProvider() {

        return Stream.of(
                null,
                Arguments.of(new User("user1", "pass1", AuthorityUtils.createAuthorityList("USER")))
        );
    }
}
