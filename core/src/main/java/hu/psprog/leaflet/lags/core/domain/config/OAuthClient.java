package hu.psprog.leaflet.lags.core.domain.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * Domain class representing an OAuth2 client.
 *
 * @author Peter Smith
 */
@Data
@Setter(AccessLevel.PACKAGE)
public class OAuthClient {

    private static final String DEFAULT_AUDIENCE = "default-audience";

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
     * Defaults to "default-audience", that can be used for UI applications.
     */
    private String audience = DEFAULT_AUDIENCE;

    /**
     * Available scopes of the registered client.
     */
    private List<String> registeredScopes = Collections.emptyList();

    /**
     * (Minimum) required scopes of the registered client.
     */
    private List<String> requiredScopes = Collections.emptyList();

    /**
     * List of the consumer services allowed to access this client.
     */
    private List<OAuthClientAllowRelation> allowedClients = Collections.emptyList();

    /**
     * List of allowed callbacks (for UI applications).
     */
    private List<String> allowedCallbacks = Collections.emptyList();
}
