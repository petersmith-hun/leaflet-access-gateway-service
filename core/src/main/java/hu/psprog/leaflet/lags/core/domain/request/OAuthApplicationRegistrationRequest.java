package hu.psprog.leaflet.lags.core.domain.request;

import hu.psprog.leaflet.lags.core.domain.config.ApplicationType;
import hu.psprog.leaflet.lags.core.domain.request.validation.ValidApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.domain.request.validation.ValidResourceServerApplication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

/**
 * Request model for creating an OAuth application.
 *
 * @param name unique name
 * @param clientID unique OAuth client ID
 * @param registrationType determines the primary OAuth application role
 * @param client settings for an OAuth application acting as a client only (top-level applications like UIs, CLIs, etc.)
 * @param resourceServer settings for an OAuth application acting as a resource server (downstream services)
 */
@Builder
@Jacksonized
@ValidApplicationRegistrationRequest
public record OAuthApplicationRegistrationRequest(

        @NotEmpty @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9-_]+$", message = "Must match pattern: ^[a-zA-Z][a-zA-Z0-9-_]+$")
        String name,

        @NotEmpty @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9-_]+$", message = "Must match pattern: ^[a-zA-Z][a-zA-Z0-9-_]+$")
        String clientID,

        @NotNull
        RegistrationType registrationType,

        @Valid
        ClientApplication client,

        @Valid
        ResourceServerApplication resourceServer
) {

    /**
     * Supported OAuth application types.
     */
    @Getter
    public enum RegistrationType {

        /**
         * Application acts as a client application only (e.g. a frontend or CLI application). Required permissions (as
         * client) are mandatory, callbacks are optional (i.e. CLI applications may not need callbacks).
         */
        CLIENT(ApplicationType.UI),

        /**
         * Application acts as a resource server only (backend service with no dependence). Resource server
         * configuration is mandatory.
         */
        RESOURCE_SERVER(ApplicationType.SERVICE),

        /**
         * Application acts as a resource server, communicating with other OAuth resource servers. Required permissions
         * (as client) are mandatory, callbacks are not applicable. Resource server configuration is mandatory.
         */
        MIDDLE_RESOURCE_SERVER(ApplicationType.SERVICE);

        private final ApplicationType applicationType;

        RegistrationType(ApplicationType applicationType) {
            this.applicationType = applicationType;
        }
    }

    /**
     * Request model representing the client settings of an OAuth application.
     *
     * @param allowedCallbacks list of allowed OAuth Authorization Code Flow return URLs (callbacks)
     * @param requiredPermissions permissions that the registered application minimally requires
     */
    @Builder
    @Jacksonized
    public record ClientApplication(

            List<@Valid AllowedCallback> allowedCallbacks,

            @NotEmpty
            List<UUID> requiredPermissions
    ) {

        /**
         * Request model representing an allowed callback.
         *
         * @param id ID (for edit requests)
         * @param url allowed callback URL
         */
        @Builder
        @Jacksonized
        public record AllowedCallback(

                UUID id,

                @NotEmpty
                @Pattern(regexp = "^http(s)?://.*")
                String url
        ) { }
    }

    /**
     * Request model representing resource server settings of an OAuth application.
     *
     * @param audience audience of the resource server application
     * @param registeredPermissions permissions known by this OAuth application
     * @param allowedClients list of client applications allowed to access this service
     */
    @Builder
    @Jacksonized
    @ValidResourceServerApplication
    public record ResourceServerApplication(

            @NotEmpty
            String audience,

            @NotEmpty
            List<UUID> registeredPermissions,

            @NotEmpty
            List<@Valid AllowedClient> allowedClients
    ) {

        /**
         * Request model representing an allowed client of an OAuth application.
         *
         * @param applicationID ID of the referenced (client or upstream service) application
         * @param allowedPermissions list of allowed permission for this client
         */
        @Builder
        @Jacksonized
        public record AllowedClient(

                @NotNull
                UUID applicationID,

                @NotEmpty
                List<UUID> allowedPermissions
        ) { }
    }
}
