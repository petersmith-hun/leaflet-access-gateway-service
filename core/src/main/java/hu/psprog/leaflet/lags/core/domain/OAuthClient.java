package hu.psprog.leaflet.lags.core.domain;

import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Domain class representing an OAuth2 client.
 *
 * @author Peter Smith
 */
@Data
public class OAuthClient {

    /**
     * Client name (internal identifier of the registration).
     */
    private final String clientName;

    /**
     * Type of this application. "UI" client applications must use authorization code flow,
     * "service" client applications can use client credentials flow.
     */
    private final ApplicationType applicationType;

    /**
     * OAuth2 client ID.
     */
    private final String clientId;

    /**
     * OAuth2 client secret.
     */
    private final String clientSecret;

    /**
     * OAuth2 audience (external client identifier for consumers).
     */
    private final String audience;

    /**
     * Available scopes of the registered client.
     */
    private final List<String> registeredScopes;

    /**
     * (Minimum) required scopes of the registered client.
     */
    private final List<String> requiredScopes;

    /**
     * List of the consumer services allowed to access this client.
     */
    private final List<OAuthClientAllowRelation> allowedClients;

    /**
     * List of allowed callbacks (for UI applications).
     */
    private final List<String> allowedCallbacks;

    public OAuthClient(String clientName, ApplicationType applicationType,
                       String clientId, String clientSecret, String audience,
                       List<String> registeredScopes, List<String> requiredScopes,
                       List<OAuthClientAllowRelation> allowedClients, List<String> allowedCallbacks) {
        this.clientName = clientName;
        this.applicationType = applicationType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
        this.registeredScopes = safeInit(registeredScopes);
        this.requiredScopes = safeInit(requiredScopes);
        this.allowedClients = safeInit(allowedClients);
        this.allowedCallbacks = safeInit(allowedCallbacks);
    }

    private <T> List<T> safeInit(List<T> inputList) {

        return Optional
                .ofNullable(inputList)
                .orElseGet(Collections::emptyList);
    }
}
