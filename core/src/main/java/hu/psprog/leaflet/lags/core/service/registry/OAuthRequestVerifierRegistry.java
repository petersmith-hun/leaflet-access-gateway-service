package hu.psprog.leaflet.lags.core.service.registry;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthAuthorizationRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;

import java.util.List;

/**
 * Registry component handling the {@link OAuthRequestVerifier} implementations.
 *
 * @author Peter Smith
 */
public interface OAuthRequestVerifierRegistry {


    /**
     * Returns the list of registered {@link OAuthRequestVerifier} implementations for Authorization Code flow authorization request verification.
     *
     * @return the list of registered {@link OAuthRequestVerifier} implementations for {@link OAuthAuthorizationRequestContext}
     */
    List<OAuthRequestVerifier<OAuthAuthorizationRequestContext>> getAuthorizationRequestVerifiers();

    /**
     * Returns the list of registered {@link OAuthRequestVerifier} implementations for the specific OAuth token request flow by the grant type.
     *
     * @param grantType {@link GrantType} enum constant defining the currently processed OAuth token request flow
     * @return the list of registered {@link OAuthRequestVerifier} implementations for {@link OAuthTokenRequestContext} and the specific grant type
     */
    List<OAuthRequestVerifier<OAuthTokenRequestContext>> getTokenRequestVerifiers(GrantType grantType);
}
