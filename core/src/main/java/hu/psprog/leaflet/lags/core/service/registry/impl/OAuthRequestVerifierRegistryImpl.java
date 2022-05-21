package hu.psprog.leaflet.lags.core.service.registry.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of {@link OAuthRequestVerifierRegistry}.
 *
 * @author Peter Smith
 */
@Component
public class OAuthRequestVerifierRegistryImpl implements OAuthRequestVerifierRegistry {

    private final Map<GrantType, List<OAuthRequestVerifier<OAuthAuthorizationRequestContext>>> authorizationRequestVerifiers;
    private final Map<GrantType, List<OAuthRequestVerifier<OAuthTokenRequestContext>>> tokenRequestVerifiers;

    @Autowired
    public OAuthRequestVerifierRegistryImpl(List<OAuthRequestVerifier<OAuthAuthorizationRequestContext>> authorizationRequestVerifiers,
                                            List<OAuthRequestVerifier<OAuthTokenRequestContext>> tokenRequestVerifiers) {

        this.authorizationRequestVerifiers = groupValidatorsByGrantType(authorizationRequestVerifiers);
        this.tokenRequestVerifiers = groupValidatorsByGrantType(tokenRequestVerifiers);
    }

    @Override
    public List<OAuthRequestVerifier<OAuthAuthorizationRequestContext>> getAuthorizationRequestVerifiers() {
        return authorizationRequestVerifiers.get(GrantType.AUTHORIZATION_CODE);
    }

    @Override
    public List<OAuthRequestVerifier<OAuthTokenRequestContext>> getTokenRequestVerifiers(GrantType grantType) {
        return tokenRequestVerifiers.get(grantType);
    }

    private <T extends OAuthRequestContext> Map<GrantType, List<OAuthRequestVerifier<T>>> groupValidatorsByGrantType(List<OAuthRequestVerifier<T>> validators) {

        return Stream.of(GrantType.values())
                .collect(Collectors.toMap(Function.identity(), collectVerifiersForGrantType(validators)));
    }

    private <T extends OAuthRequestContext> Function<GrantType, List<OAuthRequestVerifier<T>>> collectVerifiersForGrantType(List<OAuthRequestVerifier<T>> validators) {

        return grantType -> validators.stream()
                .filter(verifier -> verifier.forGrantType().contains(grantType))
                .collect(Collectors.toList());
    }
}
