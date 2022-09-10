package hu.psprog.leaflet.lags.core.service.userdetails;

import hu.psprog.leaflet.lags.core.domain.internal.ExternalUserDefinition;
import hu.psprog.leaflet.lags.core.domain.response.SignUpStatus;
import hu.psprog.leaflet.lags.core.exception.ExternalAuthenticationException;
import hu.psprog.leaflet.lags.core.service.account.AccountRequestHandler;
import hu.psprog.leaflet.lags.core.service.userdetails.external.UserDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Custom {@link OAuth2UserService} to handle mirroring the authenticated external user accounts into the local user
 * database.
 *
 * The implementation does the following steps:
 *  1) Calls the default {@link OAuth2UserService} implementation to handle collecting the basic account information.
 *  2) Selects the proper {@link UserDataFactory} implementation based on the currently used OAuth identity provider.
 *  3) Creates an {@link ExternalUserDefinition} object by calling the selected {@link UserDataFactory} implementation.
 *  4) Passes the definition to the {@link AccountRequestHandler} implementation dealing with signing up external user accounts.
 *  5) If the account is successfully signed up (or a returning user is acknowledged), then finally loads and returns the
 *     assigned local user account.
 *
 * @author Peter Smith
 */
@Primary
@Service
public class AutoRegisteringOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService;
    private final AccountRequestHandler<ExternalUserDefinition<?>, SignUpStatus> signUpAccountRequestHandler;
    private final UserDetailsService userDetailsService;
    private final Map<String, UserDataFactory<?>> userDataFactoryMap;

    @Autowired
    public AutoRegisteringOAuth2UserService(OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService,
                                            AccountRequestHandler<ExternalUserDefinition<?>, SignUpStatus> signUpAccountRequestHandler,
                                            @Qualifier("allLocalUserUserDetailsService") UserDetailsService userDetailsService,
                                            List<UserDataFactory<?>> userDataFactoryList) {
        this.defaultOAuth2UserService = defaultOAuth2UserService;
        this.signUpAccountRequestHandler = signUpAccountRequestHandler;
        this.userDetailsService = userDetailsService;
        this.userDataFactoryMap = userDataFactoryList.stream()
                .collect(Collectors.toMap(UserDataFactory::forProvider, Function.identity()));
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        ExternalUserDefinition<?> externalUserDefinition = userDataFactoryMap
                .get(userRequest.getClientRegistration().getRegistrationId())
                .createUserDefinition(userRequest, oAuth2User);

        SignUpStatus signUpStatus = signUpAccountRequestHandler.processAccountRequest(externalUserDefinition);
        if (signUpStatus != SignUpStatus.SUCCESS) {
            throw new ExternalAuthenticationException(signUpStatus, "Failed to create local mirror for external account");
        }

        return (OAuth2User) userDetailsService.loadUserByUsername(externalUserDefinition.getEmail());
    }
}
