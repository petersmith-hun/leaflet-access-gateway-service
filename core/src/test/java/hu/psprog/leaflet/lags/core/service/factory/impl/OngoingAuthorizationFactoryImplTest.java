package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.internal.UserInfo;
import hu.psprog.leaflet.lags.core.domain.request.AuthorizationResponseType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthAuthorizationRequest;
import hu.psprog.leaflet.lags.core.service.util.ScopeNegotiator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for {@link OngoingAuthorizationFactoryImpl}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class OngoingAuthorizationFactoryImplTest {

    private static final ExtendedUser EXTENDED_USER = prepareExtendedUser();
    private static final UserInfo USER_INFO = prepareUserInfo();
    private static final OAuthAuthorizationRequest O_AUTH_AUTHORIZATION_REQUEST = prepareAuthorizationRequest();
    private static final List<String> SCOPE = Arrays.asList("write:admin", "write:users");
    private static final Duration AUTH_CODE_EXPIRATION = Duration.parse("PT1M");
    private static final String CLIENT_ID = "client-id-1";
    private static final String REDIRECT_URI = "http://localhost:9999/localhost";

    @Mock
    private ScopeNegotiator scopeNegotiator;

    @Mock
    private OAuthConfigurationProperties oAuthConfigurationProperties;

    @InjectMocks
    private OngoingAuthorizationFactoryImpl ongoingAuthorizationFactory;

    @Test
    public void shouldCreateOngoingAuthorizationPrepareObject() {

        // given
        OAuthAuthorizationRequestContext context = prepareContext();

        given(scopeNegotiator.getScope(context)).willReturn(SCOPE);
        given(oAuthConfigurationProperties.getAuthCodeExpiration()).willReturn(AUTH_CODE_EXPIRATION);

        // when
        OngoingAuthorization result = ongoingAuthorizationFactory.createOngoingAuthorization(context);

        // then
        assertOngoingAuthorization(result);
    }

    private OAuthAuthorizationRequestContext prepareContext() {

        return OAuthAuthorizationRequestContext.builder()
                .request(prepareAuthorizationRequest())
                .authenticatedUser(EXTENDED_USER)
                .build();
    }

    private void assertOngoingAuthorization(OngoingAuthorization ongoingAuthorization) {

        assertAuthorizationCode(ongoingAuthorization);
        assertThat(ongoingAuthorization.getClientID(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getClientID()));
        assertThat(ongoingAuthorization.getRedirectURI(), equalTo(O_AUTH_AUTHORIZATION_REQUEST.getRedirectURI()));
        assertThat(ongoingAuthorization.getUserInfo(), equalTo(USER_INFO));
        assertThat(ongoingAuthorization.getScope(), equalTo(SCOPE));

        long expirationInSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), ongoingAuthorization.getExpiration());
        assertThat(expirationInSeconds > 57 && expirationInSeconds <= 60, is(true));
    }

    private void assertAuthorizationCode(OngoingAuthorization ongoingAuthorization) {

        assertThat(ongoingAuthorization.getAuthorizationCode(), notNullValue());
        try {
            UUID.fromString(ongoingAuthorization.getAuthorizationCode());
        } catch (Exception e) {
            fail("Authorization code is not a valid UUID");
        }
    }

    private static ExtendedUser prepareExtendedUser() {

        return ExtendedUser.builder()
                .id(1234L)
                .username("email@dev.local")
                .name("User 1")
                .role("ADMIN")
                .build();
    }

    private static UserInfo prepareUserInfo() {

        return UserInfo.builder()
                .id(EXTENDED_USER.getId())
                .email(EXTENDED_USER.getUsername())
                .username(EXTENDED_USER.getName())
                .role(EXTENDED_USER.getRole())
                .build();
    }

    private static OAuthAuthorizationRequest prepareAuthorizationRequest() {

        return OAuthAuthorizationRequest.builder()
                .responseType(AuthorizationResponseType.CODE)
                .clientID(CLIENT_ID)
                .redirectURI(REDIRECT_URI)
                .state("state-1")
                .scope("write:admin write:users")
                .build();
    }
}
