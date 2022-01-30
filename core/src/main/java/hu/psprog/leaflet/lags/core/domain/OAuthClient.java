package hu.psprog.leaflet.lags.core.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

/**
 * Domain class representing an OAuth2 client.
 *
 * @author Peter Smith
 */
@Data
@ConstructorBinding
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
     * List of the consumer services allowed to access this client.
     */
    private final List<OAuthClientAllowRelation> allowedClients;

    /**
     * List of allowed callbacks (for UI applications).
     */
    private final List<String> allowedCallbacks;
}
