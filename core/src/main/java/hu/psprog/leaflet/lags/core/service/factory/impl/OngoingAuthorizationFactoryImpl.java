package hu.psprog.leaflet.lags.core.service.factory.impl;

import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import hu.psprog.leaflet.lags.core.domain.internal.ExtendedUser;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.internal.UserInfo;
import hu.psprog.leaflet.lags.core.service.factory.OngoingAuthorizationFactory;
import hu.psprog.leaflet.lags.core.service.util.ScopeNegotiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of {@link OngoingAuthorizationFactory}.
 *
 * @author Peter Smith
 */
@Component
public class OngoingAuthorizationFactoryImpl implements OngoingAuthorizationFactory {

    private final ScopeNegotiator scopeNegotiator;
    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    @Autowired
    public OngoingAuthorizationFactoryImpl(ScopeNegotiator scopeNegotiator, OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.scopeNegotiator = scopeNegotiator;
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    @Override
    public OngoingAuthorization createOngoingAuthorization(OAuthAuthorizationRequestContext context) {

        return OngoingAuthorization.builder()
                .authorizationCode(UUID.randomUUID().toString())
                .clientID(context.getRequest().getClientID())
                .redirectURI(context.getRequest().getRedirectURI())
                .userInfo(createUserInfo(context.getAuthenticatedUser()))
                .expiration(getExpiration())
                .scope(scopeNegotiator.getScope(context))
                .build();
    }

    private UserInfo createUserInfo(ExtendedUser userDetails) {

        return UserInfo.builder()
                .id(userDetails.getId())
                .email(userDetails.getUsername())
                .username(userDetails.getName())
                .role(userDetails.getRole())
                .build();
    }

    private LocalDateTime getExpiration() {

        return LocalDateTime.now()
                .plus(oAuthConfigurationProperties.getAuthCodeExpiration());
    }
}
