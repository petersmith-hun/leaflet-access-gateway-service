package hu.psprog.leaflet.lags.core.service.userdetails.external.impl;

import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.service.userdetails.external.UserDataFactory;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * {@link UserDataFactory} implementation handling external user definition creation for users coming from Google.
 *
 * The implementation collects the following information:
 *  - Account type will be {@link AccountType#GOOGLE}.
 *  - User ID (external) is collected from the "sub" OAuth user attribute.
 *  - Username is collected from the "name" OAuth user attribute.
 *  - Email address is collected from the "email" OAuth user attribute.
 *
 *
 * @author Peter Smith
 */
@Component
public class GoogleUserDataFactory implements UserDataFactory<String> {

    private static final String PROVIDER_NAME = "google";
    private static final String ATTRIBUTE_USER_ID = "sub";
    private static final String ATTRIBUTE_USERNAME = "name";
    private static final String ATTRIBUTE_EMAIL = "email";

    @Override
    public ExternalUserDefinition<String> createUserDefinition(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        return ExternalUserDefinition.<String>builder()
                .accountType(AccountType.GOOGLE)
                .userID(extractUserID(oAuth2User))
                .username(extractUsername(oAuth2User))
                .email(extractEmail(oAuth2User))
                .build();
    }

    @Override
    public String forProvider() {
        return PROVIDER_NAME;
    }

    private String extractUserID(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(ATTRIBUTE_USER_ID);
    }

    private String extractUsername(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(ATTRIBUTE_USERNAME);
    }

    private String extractEmail(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(ATTRIBUTE_EMAIL);
    }
}
