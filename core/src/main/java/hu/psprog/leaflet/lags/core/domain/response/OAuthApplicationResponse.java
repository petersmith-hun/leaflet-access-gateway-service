package hu.psprog.leaflet.lags.core.domain.response;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response model representing an OAuth application definition.
 *
 * @param id generated internal ID of the application
 * @param name unique name
 * @param clientID unique OAuth client ID
 * @param applicationType internal OAuth application type (primary OAuth role)
 * @param client client application settings
 * @param resourceServer resource server application settings
 * @param enabled determines if the definition can be requested by OAuth application logins
 * @param created creation timestamp
 * @param lastModified last modification timestamp
 */
@Builder
public record OAuthApplicationResponse(
        UUID id,
        String name,
        String clientID,
        ApplicationType applicationType,
        ClientApplication client,
        ResourceServerApplication resourceServer,
        boolean enabled,
        ZonedDateTime created,
        ZonedDateTime lastModified
) {

    /**
     * Response model representing the settings of an OAuth application acting as a client only (top level applications).
     *
     * @param allowedCallbacks list of allowed callbacks
     * @param requiredPermissions list of permissions minimally required by the application
     * @param resourceServers immediate resource servers used by this client application
     */
    @Builder
    public record ClientApplication(
            List<Callback> allowedCallbacks,
            List<Permission> requiredPermissions,
            List<TargetApplication> resourceServers
    ) {

        /**
         * Response model representing a callback URL.
         *
         * @param id generated internal ID
         * @param url the URL for the application to be redirected to on successful login
         */
        @Builder
        public record Callback(
                UUID id,
                String url
        ) { }
    }

    /**
     * Response model representing the settings of an OAuth application acting as a resource server (downstream services).
     *
     * @param audience OAuth audience value of this application
     * @param registeredPermissions list of permissions known by this application
     * @param allowedClients list of client applications that may access this resource server
     */
    @Builder
    public record ResourceServerApplication(
            String audience,
            List<Permission> registeredPermissions,
            List<AllowedClient> allowedClients
    ) {

        /**
         * Response model representing an allowed client relation for resource server applications.
         *
         * @param id generated internal ID of this relation
         * @param application referenced client application
         * @param allowedPermissions list of permissions allowed by this relation
         */
        @Builder
        public record AllowedClient(
                UUID id,
                TargetApplication application,
                List<Permission> allowedPermissions
        ) { }
    }

    /**
     * Short representation of a referenced permissions.
     *
     * @param id generated internal ID
     * @param name name of the permission
     * @param description optional description
     */
    @Builder
    public record Permission(
            UUID id,
            String name,
            String description
    ) { }

    /**
     * Short representation of a referenced application.
     *
     * @param id generated internal ID
     * @param name unique name of the application definition
     * @param clientID unique OAuth client Id of the referenced application
     */
    @Builder
    public record TargetApplication(
            UUID id,
            String name,
            String clientID
    ) { }
}
