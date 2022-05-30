package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthTokenRequestException;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * {@link OAuthRequestVerifier} implementation for OAuth Client Credentials flow token requests.
 * The implementation verifies the following aspects:
 *  - Scope (requested scope, must be specified and must not be empty).
 *
 * @author Peter Smith
 */
@Component
public class ClientCredentialsTokenOAuthRequestVerifier implements OAuthRequestVerifier<OAuthTokenRequestContext> {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.CLIENT_CREDENTIALS);

    @Override
    public void verify(OAuthTokenRequestContext context) {

        List<String> scope = context.getRequest().getScope();
        if (Objects.isNull(scope) || scope.isEmpty()) {
            throw new OAuthTokenRequestException(OAuthErrorCode.INVALID_SCOPE, "Value for required authorization parameter [scope] is missing");
        }
    }

    @Override
    public List<GrantType> forGrantType() {
        return GRANT_TYPES;
    }
}
