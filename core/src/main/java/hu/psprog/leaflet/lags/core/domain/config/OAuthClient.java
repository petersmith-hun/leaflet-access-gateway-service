package hu.psprog.leaflet.lags.core.domain.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Domain class representing an OAuth2 client.
 *
 * @author Peter Smith
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClient {

    /**
     * Client name (internal identifier of the registration).
     */
    private String clientName;

    /**
     * Type of this application. "UI" client applications must use authorization code flow,
     * "service" client applications can use client credentials flow.
     */
    private ApplicationType applicationType;

    /**
     * OAuth2 client ID.
     */
    private String clientId;

    /**
     * OAuth2 client secret.
     */
    private String clientSecret;

    /**
     * OAuth2 audience (external client identifier for consumers).
     */
    private String audience;

    /**
     * Available scopes of the registered client.
     */
    @Builder.Default
    private List<String> registeredScopes = Collections.emptyList();

    /**
     * (Minimum) required scopes of the registered client.
     */
    @Builder.Default
    private List<String> requiredScopes = Collections.emptyList();

    /**
     * List of the consumer services allowed to access this client.
     */
    @Builder.Default
    private List<OAuthClientAllowRelation> allowedClients = Collections.emptyList();

    /**
     * List of allowed callbacks (for UI applications).
     */
    @Builder.Default
    private List<String> allowedCallbacks = Collections.emptyList();
}
