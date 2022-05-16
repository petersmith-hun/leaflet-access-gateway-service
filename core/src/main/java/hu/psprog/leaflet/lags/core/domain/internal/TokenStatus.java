package hu.psprog.leaflet.lags.core.domain.internal;

/**
 * Possible statuses of the followed access tokens.
 *
 * @author Peter Smith
 */
public enum TokenStatus {

    /**
     * Token is active and valid.
     */
    ACTIVE,

    /**
     * Token has been revoked during its active lifecycle.
     * Basically user has signed out.
     */
    REVOKED
}
