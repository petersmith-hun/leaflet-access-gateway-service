package hu.psprog.leaflet.lags.core.service.processor.verifier.impl;

import hu.psprog.leaflet.lags.core.domain.internal.OAuthTokenRequestContext;
import hu.psprog.leaflet.lags.core.domain.internal.OngoingAuthorization;
import hu.psprog.leaflet.lags.core.domain.request.GrantType;
import hu.psprog.leaflet.lags.core.domain.request.OAuthTokenRequest;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;
import hu.psprog.leaflet.lags.core.persistence.repository.OngoingAuthorizationRepository;
import hu.psprog.leaflet.lags.core.service.processor.verifier.OAuthRequestVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link OAuthRequestVerifier} implementation to verify {@link OngoingAuthorization} object on Authorization Code flow token requests.
 * The implementation verifies the following aspects:
 *  - Existence of the {@link OngoingAuthorization} object;
 *  - Authorized Client ID (stored one in the {@link OngoingAuthorization} must be the same as the requested one in the token request);
 *  - Authorized Redirect URI (stored one must be the same as the requested one);
 *  - Expiration of the authorization code (expired codes must be rejected).
 *
 * @author Peter Smith
 */
@Component
public class OngoingAuthorizationOAuthRequestVerifier implements OAuthRequestVerifier<OAuthTokenRequestContext> {

    private static final List<GrantType> GRANT_TYPES = List.of(GrantType.AUTHORIZATION_CODE);

    private final OngoingAuthorizationRepository ongoingAuthorizationRepository;

    @Autowired
    public OngoingAuthorizationOAuthRequestVerifier(OngoingAuthorizationRepository ongoingAuthorizationRepository) {
        this.ongoingAuthorizationRepository = ongoingAuthorizationRepository;
    }

    @Override
    public void verify(OAuthTokenRequestContext context) {

        if (context.getOngoingAuthorization().isEmpty()) {
            throw new OAuthAuthorizationException("Unknown authorization request");
        }

        context.getOngoingAuthorization()
                .ifPresent(ongoingAuthorization -> {
                    verifyClientID(ongoingAuthorization, context.getRequest());
                    verifyRedirectURI(ongoingAuthorization, context.getRequest());
                    verifyExpiration(ongoingAuthorization);
                });
    }

    @Override
    public List<GrantType> forGrantType() {
        return GRANT_TYPES;
    }

    private void verifyClientID(OngoingAuthorization ongoingAuthorization, OAuthTokenRequest oAuthTokenRequest) {

        if (!ongoingAuthorization.getClientID().equals(oAuthTokenRequest.getClientID())) {
            signalInvalidOngoingAuthorization(ongoingAuthorization, "Authorization request belongs to a different client.");
        }
    }

    private void verifyRedirectURI(OngoingAuthorization ongoingAuthorization, OAuthTokenRequest oAuthTokenRequest) {

        if (!ongoingAuthorization.getRedirectURI().equals(oAuthTokenRequest.getRedirectURI())) {
            signalInvalidOngoingAuthorization(ongoingAuthorization, "Different redirect URI has been specified in the token request.");
        }
    }

    private void verifyExpiration(OngoingAuthorization ongoingAuthorization) {

        if (ongoingAuthorization.getExpiration().isBefore(LocalDateTime.now())) {
            signalInvalidOngoingAuthorization(ongoingAuthorization, "Authorization has already expired.");
        }
    }

    private void signalInvalidOngoingAuthorization(OngoingAuthorization ongoingAuthorization, String message) {

        ongoingAuthorizationRepository.deleteOngoingAuthorization(ongoingAuthorization.getAuthorizationCode());
        throw new OAuthAuthorizationException(message);
    }
}
