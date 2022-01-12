package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.GrantType;
import hu.psprog.leaflet.lags.core.domain.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.service.util.OAuthClientRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * {@link AbstractGrantFlowProcessor} implementation for client credentials grant OAuth2 authorization flow processing.
 * Since the common ({@link AbstractGrantFlowProcessor}) implementation does all the necessary checks,
 * this processor only needs to some scope verification.
 *
 * @author Peter Smith
 */
@Component
public class ClientCredentialsGrantFlowProcessor extends AbstractGrantFlowProcessor {

    @Autowired
    public ClientCredentialsGrantFlowProcessor(OAuthClientRegistry oAuthClientRegistry) {
        super(oAuthClientRegistry);
    }

    @Override
    public GrantType forGrantType() {
        return GrantType.CLIENT_CREDENTIALS;
    }

    @Override
    protected void doFlowSpecificVerification(OAuthTokenRequest oAuthTokenRequest, OAuthClient oAuthClient) {

        if (Objects.isNull(oAuthTokenRequest.getScope()) || oAuthTokenRequest.getScope().isEmpty()) {
            throw new OAuthAuthorizationException("Value for required authorization parameter [scope] is missing");
        }
    }
}
