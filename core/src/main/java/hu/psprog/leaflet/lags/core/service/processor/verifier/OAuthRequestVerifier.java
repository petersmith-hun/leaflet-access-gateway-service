package hu.psprog.leaflet.lags.core.service.processor.verifier;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

import java.util.List;

/**
 * OAuth authorization request and token request verifier components.
 *
 * Implementations of this interface should verify a specific parameter or set of parameters. Every implementation
 * should signal the failure with an {@link OAuthAuthorizationException}.
 *
 * @author Peter Smith
 */
public interface OAuthRequestVerifier<T extends OAuthRequestContext> {

    /**
     * Verifies an aspect of the given context object.
     *
     * @param context {@link OAuthTokenRequestContext} object containing the token request parameters
     * @throws OAuthAuthorizationException on verification failure
     */
    void verify(T context);

    /**
     * Returns the list of grant types as {@link GrantType} enum constants for which this verifier is applicable.
     *
     * @return the list of grant types as {@link GrantType} enum constants
     */
    List<GrantType> forGrantType();
}
