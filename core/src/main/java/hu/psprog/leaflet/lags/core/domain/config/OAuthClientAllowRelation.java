package hu.psprog.leaflet.lags.core.domain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.List;

/**
 * Client relationship descriptor.
 * Defines the client name and the list of allowed scopes for a given registered client.
 *
 * @author Peter Smith
 */
@Data
@ConstructorBinding
public class OAuthClientAllowRelation {

    /**
     * Consumer service name (internal registration identifier).
     */
    private final String name;

    /**
     * Scopes this client allows the consumer to access.
     */
    private final List<String> allowedScopes;
}
