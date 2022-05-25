package hu.psprog.leaflet.lags.core.domain.entity;

/**
 * User roles.
 *
 * @author Peter Smith
 */
public enum Role {

    /**
     * Default user role for visitors.
     */
    USER,

    /**
     * Blog editors.
     */
    EDITOR,

    /**
     * Administrators.
     */
    ADMIN,

    /**
     * Virtual users used by external services, like CBFS.
     */
    SERVICE,

    /**
     * Non-registered user type, which is created when a user comments without registration.
     * These users are not allowed to log in, though their role can be elevated to USER.
     */
    NO_LOGIN
}
