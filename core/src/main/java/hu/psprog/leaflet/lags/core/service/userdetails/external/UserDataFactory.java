package hu.psprog.leaflet.lags.core.service.userdetails.external;

import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Implementations of this interface should be able to create {@link ExternalUserDefinition} objects, that can be used
 * to sign-up users coming from external providers (such as GitHub, Google, etc.).
 *
 * @param <ID> type of the primary ID of the users (passed to the {@link ExternalUserDefinition} object)
 * @author Peter Smith
 */
public interface UserDataFactory<ID> {

    /**
     * Creates an {@link ExternalUserDefinition} object based on the {@link OAuth2UserRequest} (mainly needed to determine
     * the OAuth identity provider registration name) and the authenticated {@link OAuth2User} object (in order for the
     * factories to be able to collect the necessary user attributes).
     *
     * @param userRequest {@link OAuth2UserRequest} object
     * @param oAuth2User {@link OAuth2User} object
     * @return created user definition as {@link ExternalUserDefinition}
     */
    ExternalUserDefinition<ID> createUserDefinition(OAuth2UserRequest userRequest, OAuth2User oAuth2User);

    /**
     * Returns the name of the assigned OAuth identity provider. The returned name must match the OAuth provider registration
     * name in the security configuration.
     *
     * @return name of the assigned OAuth identity provider
     */
    String forProvider();
}
