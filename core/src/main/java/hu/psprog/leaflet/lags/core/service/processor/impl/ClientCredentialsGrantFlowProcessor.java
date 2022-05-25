package hu.psprog.leaflet.lags.core.service.processor.impl;

import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.service.registry.OAuthRequestVerifierRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link AbstractGrantFlowProcessor} implementation for client credentials grant OAuth2 authorization flow processing.
 * Since the common ({@link AbstractGrantFlowProcessor}) implementation does all the necessary checks,
 * this processor only needs to do scope verification.
 *
 * @author Peter Smith
 */
@Component
public class ClientCredentialsGrantFlowProcessor extends AbstractGrantFlowProcessor {

    @Autowired
    public ClientCredentialsGrantFlowProcessor(OAuthRequestVerifierRegistry oAuthRequestVerifierRegistry) {
        super(oAuthRequestVerifierRegistry);
    }

    @Override
    public GrantType forGrantType() {
        return GrantType.CLIENT_CREDENTIALS;
    }
}
