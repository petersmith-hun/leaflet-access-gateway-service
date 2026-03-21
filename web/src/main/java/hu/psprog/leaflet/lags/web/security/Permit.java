package hu.psprog.leaflet.lags.web.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Supported permissions by the REST API endpoints.
 *
 * @author Peter Smith
 */
public interface Permit {

    /**
     * Read permissions.
     */
    interface Read {

        /**
         * Allows reading OAuth application definitions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:access:applications')")
        @interface Applications { }

        /**
         * Allows reading permissions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:access:permissions')")
        @interface Permissions { }

        /**
         * Allows reading own profile of the active user.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:access:profile') || hasAuthority('read:access:profile')")
        @interface Profile { }

        /**
         * Allows reading roles.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:access:roles')")
        @interface Roles { }

        /**
         * Allows reading users.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:access:users')")
        @interface Users { }
    }

    /**
     * Write permissions.
     */
    interface Write {

        /**
         * Allows writing (creating, editing and deleting) OAuth application definitions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:applications')")
        @interface Applications { }

        /**
         * Allows writing (creating, editing and deleting) permissions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:permissions')")
        @interface Permissions { }

        /**
         * Allows modifying own profile of the active user.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:profile') || hasAuthority('write:access:profile')")
        @interface Profile { }

        /**
         * Allows deleting own profile of the active user.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:profile:delete') || hasAuthority('write:access:profile:delete')")
        @interface ProfileDelete { }

        /**
         * Allows writing (creating, editing and deleting) roles.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:roles')")
        @interface Roles { }

        /**
         * Allows writing (creating, changing role of, and enabling/disabling) users.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:access:users')")
        @interface Users { }

        /**
         * Allows resetting the password.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:reclaim')")
        @interface PasswordReset { }
    }
}
