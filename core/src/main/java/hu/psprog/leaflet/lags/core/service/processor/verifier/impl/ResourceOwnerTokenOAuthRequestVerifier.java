package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * {@link OAuthTokenRequestFieldExistenceVerifier} verifier implementation for OAuth Resource Owner (Password Grant) flow verification.
 * The implementation verifies the existence of the following fields in the context:
 *  - Username (email address of the identified user);
 *  - Password (password of the identified user).
 *
 * @author Peter Smith
 */
@Component
public class ResourceOwnerTokenOAuthRequestVerifier extends OAuthTokenRequestFieldExistenceVerifier {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.PASSWORD);

    @Override
    public void verify(OAuthTokenRequestContext context) {
        verifyFieldExistence(context);
    }

    @Override
    public List<GrantType> forGrantType() {
        return GRANT_TYPES;
    }

    private void verifyFieldExistence(OAuthTokenRequestContext context) {

        verifyFieldExistence(context.getRequest(), Map.of(
                OAuthConstants.Request.USERNAME, OAuthTokenRequest::getUsername,
                OAuthConstants.Request.PASSWORD, OAuthTokenRequest::getPassword
        ));
    }
}
