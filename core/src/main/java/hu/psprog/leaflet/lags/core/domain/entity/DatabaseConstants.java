package hu.psprog.leaflet.lags.core.domain.entity;

/**
 * Database constants used in entities.
 *
 * @author Peter Smith
 */
public final class DatabaseConstants {

    static final String TABLE_PREFIX = "leaflet_";

    static final String TABLE_USERS = TABLE_PREFIX + "users";

    static final String COLUMN_DATE_CREATED = "date_created";
    static final String COLUMN_DATE_LAST_MODIFIED = "date_last_modified";
    static final String COLUMN_IS_ENABLED = "is_enabled";
    static final String COLUMN_USERNAME = "username";
    static final String COLUMN_EMAIL = "email";
    static final String COLUMN_ROLE = "role";
    static final String COLUMN_PASSWORD = "password";
    static final String COLUMN_DEFAULT_LOCALE = "default_locale";
    static final String COLUMN_ACCOUNT_TYPE = "account_type";
    static final String COLUMN_DATE_LAST_LOGIN = "date_last_login";
    static final String COLUMN_EXTERNAL_ID = "external_id";

    static final String UK_USER_EMAIL = "UK_USER_EMAIL";

    private DatabaseConstants() {}
}
