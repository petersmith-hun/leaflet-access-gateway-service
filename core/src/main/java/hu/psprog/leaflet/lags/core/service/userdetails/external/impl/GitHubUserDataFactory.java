package hu.psprog.leaflet.lags.core.service.userdetails.external.impl;

import hu.psprog.leaflet.bridge.client.BridgeClient;
import hu.psprog.leaflet.bridge.client.domain.BridgeService;
import hu.psprog.leaflet.bridge.client.exception.CommunicationFailureException;
import hu.psprog.leaflet.bridge.client.exception.DefaultNonSuccessfulResponseException;
import hu.psprog.leaflet.bridge.client.request.RESTRequest;
import hu.psprog.leaflet.bridge.client.request.RequestMethod;
import hu.psprog.leaflet.lags.core.domain.entity.AccountType;
import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.internal.GitHubEmailItem;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import hu.psprog.leaflet.lags.core.service.userdetails.external.UserDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * {@link UserDataFactory} implementation handling external user definition creation for users coming from GitHub.
 *
 * The implementation collects the following information:
 *  - Account type will be {@link AccountType#GITHUB}.
 *  - User ID (external) is collected from the "id" OAuth user attribute.
 *  - Username is collected from the "name" OAuth user attribute.
 *  - Email address is collected using GitHub's {@code /user/emails} API endpoint, by extracting the user's primary email address.
 *
 * @author Peter Smith
 */
@BridgeService(client = "github")
@Slf4j
public class GitHubUserDataFactory implements UserDataFactory<Long> {

    private static final String PROVIDER_NAME = "github";
    private static final String ATTRIBUTE_USER_ID = "id";
    private static final String ATTRIBUTE_USERNAME = "name";

    private static final GenericType<List<GitHubEmailItem>> GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE = new GenericType<>() {};
    private static final String PATH_USER_EMAILS = "/user/emails";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER_TOKEN_TEMPLATE = "Bearer %s";

    private final BridgeClient bridgeClient;

    @Autowired
    public GitHubUserDataFactory(BridgeClient bridgeClient) {
        this.bridgeClient = bridgeClient;
    }

    @Override
    public ExternalUserDefinition<Long> createUserDefinition(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        return ExternalUserDefinition.<Long>builder()
                .accountType(AccountType.GITHUB)
                .userID(extractUserID(oAuth2User))
                .username(extractUsername(oAuth2User))
                .email(getPrimaryEmail(userRequest))
                .build();
    }

    @Override
    public String forProvider() {
        return PROVIDER_NAME;
    }

    private Long extractUserID(OAuth2User oAuth2User) {

        return Optional.ofNullable(oAuth2User.getAttribute(ATTRIBUTE_USER_ID))
                .map(Integer.class::cast)
                .map(Integer::longValue)
                .orElse(0L);
    }

    private String extractUsername(OAuth2User oAuth2User) {
        return oAuth2User.getAttribute(ATTRIBUTE_USERNAME);
    }

    private String getPrimaryEmail(OAuth2UserRequest userRequest) {

        return retrieveEmailAddresses(userRequest)
                .stream()
                .filter(GitHubEmailItem::isPrimary)
                .map(GitHubEmailItem::getEmail)
                .findFirst()
                .orElseThrow(supplyAuthenticationException());
    }

    private List<GitHubEmailItem> retrieveEmailAddresses(OAuth2UserRequest userRequest) {

        try {
            return bridgeClient.call(createEmailRetrievalRequest(userRequest), GITHUB_API_EMAIL_ENDPOINT_GENERIC_TYPE);
        } catch (DefaultNonSuccessfulResponseException | CommunicationFailureException exception) {
            log.error("Could not contact GitHub API to retrieve the user's primary address address", exception);
            throw supplyAuthenticationException().get();
        }
    }

    private RESTRequest createEmailRetrievalRequest(OAuth2UserRequest userRequest) {

        return RESTRequest.getBuilder()
                .method(RequestMethod.GET)
                .path(() -> PATH_USER_EMAILS)
                .addHeaderParameter(HEADER_AUTHORIZATION, String.format(BEARER_TOKEN_TEMPLATE, userRequest.getAccessToken().getTokenValue()))
                .build();
    }

    private Supplier<ExternalAuthenticationException> supplyAuthenticationException() {
        return () -> new ExternalAuthenticationException(SignUpStatus.FAILURE, "Could not retrieve primary email address from the user's GitHub account");
    }
}
