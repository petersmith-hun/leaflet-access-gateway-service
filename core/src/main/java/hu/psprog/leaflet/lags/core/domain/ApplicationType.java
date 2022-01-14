package hu.psprog.leaflet.lags.core.domain;

/**
 * Possible types of the registered OAuth clients.
 *
 * @author Peter Smith
 */
public enum ApplicationType {

    /**
     * User-facing application.
     * These applications must request authorization using the Authorization Code Flow.
     */
    UI,

    /**
     * Backend service application.
     * These applications can utilize Client Credentials Flow to acquire service-to-service authorization tokens.
     */
    SERVICE
}
