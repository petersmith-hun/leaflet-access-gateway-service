package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.userdetails.external.UserDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link AutoRegisteringOAuth2UserService}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class AutoRegisteringOAuth2UserServiceTest {

    private static final String FIRST_REGISTRATION_ID = "first";
    private static final String SECOND_REGISTRATION_ID = "second";
    private static final String EXTERNAL_USER_EMAIL = "externaluser1@dev.local";

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;

    @Mock
    private AccountRequestHandler<ExternalUserDefinition<?>, SignUpStatus> signUpAccountRequestHandler;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserDataFactory<Long> firstUserDataFactory;

    @Mock
    private UserDataFactory<Long> secondUserDataFactory;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private ExternalUserDefinition<Long> externalUserDefinition;

    @Mock
    private ExtendedUser localOAuth2User;

    private AutoRegisteringOAuth2UserService autoRegisteringOAuth2UserService;

    @BeforeEach
    public void setup() {

        given(firstUserDataFactory.forProvider()).willReturn(FIRST_REGISTRATION_ID);
        given(secondUserDataFactory.forProvider()).willReturn(SECOND_REGISTRATION_ID);

        autoRegisteringOAuth2UserService = new AutoRegisteringOAuth2UserService(defaultOAuth2UserService, signUpAccountRequestHandler,
                userDetailsService, List.of(firstUserDataFactory, secondUserDataFactory));
    }

    @Test
    public void shouldLoadUserSuccessfullyProcessExternalUserUsingFirstUserDataFactory() {

        // given
        OAuth2UserRequest userRequest = prepareUserRequest(FIRST_REGISTRATION_ID);

        given(defaultOAuth2UserService.loadUser(userRequest)).willReturn(oAuth2User);
        given(firstUserDataFactory.createUserDefinition(userRequest, oAuth2User)).willReturn(externalUserDefinition);
        given(signUpAccountRequestHandler.processAccountRequest(externalUserDefinition)).willReturn(SignUpStatus.SUCCESS);
        given(externalUserDefinition.getEmail()).willReturn(EXTERNAL_USER_EMAIL);
        given(userDetailsService.loadUserByUsername(EXTERNAL_USER_EMAIL)).willReturn(localOAuth2User);

        // when
        OAuth2User result = autoRegisteringOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result, equalTo(localOAuth2User));
    }

    @Test
    public void shouldLoadUserSuccessfullyProcessExternalUserUsingSecondUserDataFactory() {

        // given
        OAuth2UserRequest userRequest = prepareUserRequest(SECOND_REGISTRATION_ID);

        given(defaultOAuth2UserService.loadUser(userRequest)).willReturn(oAuth2User);
        given(secondUserDataFactory.createUserDefinition(userRequest, oAuth2User)).willReturn(externalUserDefinition);
        given(signUpAccountRequestHandler.processAccountRequest(externalUserDefinition)).willReturn(SignUpStatus.SUCCESS);
        given(externalUserDefinition.getEmail()).willReturn(EXTERNAL_USER_EMAIL);
        given(userDetailsService.loadUserByUsername(EXTERNAL_USER_EMAIL)).willReturn(localOAuth2User);

        // when
        OAuth2User result = autoRegisteringOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result, equalTo(localOAuth2User));
    }

    @ParameterizedTest
    @EnumSource(value = SignUpStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
    public void shouldLoadUserFailWithExceptionOnNonSuccessfulAccountRequestProcessing(SignUpStatus invalidSignUpStatus) {

        // given
        OAuth2UserRequest userRequest = prepareUserRequest(SECOND_REGISTRATION_ID);

        given(defaultOAuth2UserService.loadUser(userRequest)).willReturn(oAuth2User);
        given(secondUserDataFactory.createUserDefinition(userRequest, oAuth2User)).willReturn(externalUserDefinition);
        given(signUpAccountRequestHandler.processAccountRequest(externalUserDefinition)).willReturn(invalidSignUpStatus);

        // when
        ExternalAuthenticationException exception = assertThrows(ExternalAuthenticationException.class, () -> autoRegisteringOAuth2UserService.loadUser(userRequest));

        // then
        // exception expected
        assertThat(exception.getMessage(), equalTo("Failed to create local mirror for external account"));
        assertThat(exception.getSignUpStatus(), equalTo(invalidSignUpStatus));
    }

    private OAuth2UserRequest prepareUserRequest(String registrationID) {

        ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId(registrationID)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientId("client-id-1")
                .tokenUri("http://localhost")
                .build();

        OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token1",
                Instant.now(), Instant.now().plusSeconds(10L));

        return new OAuth2UserRequest(clientRegistration, oAuth2AccessToken, Collections.emptyMap());
    }
}
