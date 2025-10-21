package hu.psprog.leaflet.lags.core.domain.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * Client relationship descriptor.
 * Defines the client name and the list of allowed scopes for a given registered client.
 *
 * @author Peter Smith
 */
@Data
@Setter(AccessLevel.PACKAGE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientAllowRelation {

    /**
     * Consumer service name (internal registration identifier).
     */
    private String name;

    /**
     * Scopes this client allows the consumer to access.
     */
    @Builder.Default
    private List<String> allowedScopes = Collections.emptyList();
}
