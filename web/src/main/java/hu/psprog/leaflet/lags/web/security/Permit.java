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
        @PreAuthorize("hasAuthority('SCOPE_read:oauth:applications')")
        @interface Applications { }

        /**
         * Allows reading permissions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_read:permissions')")
        @interface Permissions { }
    }

    /**
     * Write permissions.
     */
    interface Write {

        /**
         * Allows writing (creating, editing and deleting) OAuth application definitions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:oauth:applications')")
        @interface Applications { }

        /**
         * Allows writing (creating, editing and deleting) permissions.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:permissions')")
        @interface Permissions { }

        /**
         * Allows resetting the password.
         */
        @Retention(RetentionPolicy.RUNTIME)
        @PreAuthorize("hasAuthority('SCOPE_write:reclaim')")
        @interface PasswordReset { }
    }
}
