package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Abstract base {@link OAuthRequestVerifier} implementation for {@link OAuthTokenRequestContext} verifiers checking field existence.
 *
 * @author Peter Smith
 */
abstract class OAuthTokenRequestFieldExistenceVerifier implements OAuthRequestVerifier<OAuthTokenRequestContext> {

    /**
     * Verifies if the specified fields in the {@link OAuthTokenRequest} are not blank (null or empty string).
     *
     * @param oAuthTokenRequest authorization request model as {@link OAuthTokenRequest}
     * @param fieldMappers field mapper function to extract specific fields of the {@link OAuthTokenRequest} object
     */
    protected final void verifyFieldExistence(OAuthTokenRequest oAuthTokenRequest, Map<String, Function<OAuthTokenRequest, String>> fieldMappers) {

        fieldMappers.forEach((fieldName, valueMapper) -> {
            String value = valueMapper.apply(oAuthTokenRequest);
            if (StringUtils.isEmpty(value)) {
                throw new OAuthAuthorizationException(String.format("Value for required authorization parameter [%s] is missing", fieldName));
            }
        });
    }
}
