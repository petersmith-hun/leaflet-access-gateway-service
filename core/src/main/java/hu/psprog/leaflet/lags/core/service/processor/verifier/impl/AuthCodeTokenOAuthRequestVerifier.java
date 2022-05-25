package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * {@link OAuthTokenRequestFieldExistenceVerifier} implementation for OAuth Authorization Code flow token requests.
 * The implementation verifies the existence of the following fields:
 *  - Code (authorization code received during the authorization step of the flow);
 *  - Redirect URI (the requested redirection URI, must be the same as the one requested during the authorization step of the flow).
 *
 * @author Peter Smith
 */
@Component
public class AuthCodeTokenOAuthRequestVerifier extends OAuthTokenRequestFieldExistenceVerifier {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.AUTHORIZATION_CODE);

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
                OAuthConstants.Request.CODE, OAuthTokenRequest::getAuthorizationCode,
                OAuthConstants.Request.REDIRECT_URI, OAuthTokenRequest::getRedirectURI
        ));
    }
}
