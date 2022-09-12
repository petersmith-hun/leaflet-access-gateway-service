package hu.psprog.leaflet.lags.core.service.userdetails.external.impl;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link GoogleUserDataFactory}.
 *
 * @author Peter Smith
 */
@ExtendWith(MockitoExtension.class)
class GoogleUserDataFactoryTest {

    private static final String USER_ID = "997744";
    private static final String USERNAME = "External User";
    private static final String EMAIL = "externaluser1-2@dev.local";
    private static final String REGISTRATION_ID = "google";

    private static final OAuth2User OAUTH_USER = prepareOAuth2User();
    private static final ExternalUserDefinition<String> EXPECTED_EXTERNAL_USER_DEFINITION = prepareExternalUserDefinition();

    @InjectMocks
    private GoogleUserDataFactory googleUserDataFactory;

    @Test
    public void shouldCreateUserDefinitionProcessRequestSuccessfully() {

        // when
        ExternalUserDefinition<String> result = googleUserDataFactory.createUserDefinition(null, OAUTH_USER);

        // then
        assertThat(result, equalTo(EXPECTED_EXTERNAL_USER_DEFINITION));
    }

    @Test
    public void shouldForProviderReturnGoogle() {

        // when
        String result = googleUserDataFactory.forProvider();

        // then
        assertThat(result, equalTo(REGISTRATION_ID));
    }

    private static OAuth2User prepareOAuth2User() {

        return new DefaultOAuth2User(Collections.emptyList(), Map.of(
                "sub", USER_ID,
                "name", USERNAME,
                "email", EMAIL
        ), "sub");
    }

    private static ExternalUserDefinition<String> prepareExternalUserDefinition() {

        return ExternalUserDefinition.<String>builder()
                .accountType(AccountType.GOOGLE)
                .userID(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .build();
    }
}
