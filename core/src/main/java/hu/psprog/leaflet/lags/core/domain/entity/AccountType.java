package hu.psprog.leaflet.lags.core.domain.entity;

/**
 * Possible user account types.
 *
 * @author Peter Smith
 */
public enum AccountType {

    /**
     * "Local" user account, the account is registered directly in the Leaflet system.
     */
    LOCAL,

    /**
     * External user account, provided by GitHub.
     */
    GITHUB,

    /**
     * External user account, provided by Google.
     */
    GOOGLE
}
