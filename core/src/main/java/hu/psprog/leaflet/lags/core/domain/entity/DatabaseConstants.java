package hu.psprog.leaflet.lags.core.domain.entity;

/**
 * Database constants used in entities.
 *
 * @author Peter Smith
 */
public final class DatabaseConstants {

    static final String TABLE_PREFIX = "leaflet_";

    static final String TABLE_OAUTH_APPLICATIONS = TABLE_PREFIX + "oauth_applications";
    static final String TABLE_OAUTH_CALLBACKS = TABLE_PREFIX + "oauth_callbacks";
    static final String TABLE_OAUTH_ALLOWED_CLIENTS = TABLE_PREFIX + "oauth_allowed_clients";
    static final String TABLE_PERMISSIONS = TABLE_PREFIX + "permissions";
    static final String TABLE_USERS = TABLE_PREFIX + "users";

    static final String COLUMN_DATE_CREATED = "date_created";
    static final String COLUMN_DATE_LAST_MODIFIED = "date_last_modified";
    static final String COLUMN_ID = "id";
    static final String COLUMN_IS_ENABLED = "is_enabled";
    static final String COLUMN_USERNAME = "username";
    static final String COLUMN_EMAIL = "email";
    static final String COLUMN_ROLE = "role";
    static final String COLUMN_PASSWORD = "password";
    static final String COLUMN_DEFAULT_LOCALE = "default_locale";
    static final String COLUMN_ACCOUNT_TYPE = "account_type";
    static final String COLUMN_DATE_LAST_LOGIN = "date_last_login";
    static final String COLUMN_EXTERNAL_ID = "external_id";
    static final String COLUMN_OAUTH_APPLICATION_ID = "oauth_application_id";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_CLIENT_ID = "client_id";
    static final String COLUMN_AUDIENCE = "audience";

    static final String UK_USER_EMAIL = "UK_USER_EMAIL";
    static final String UK_OAUTH_APPLICATION_NAME = "uk_oauth_application_name";
    static final String UK_OAUTH_APPLICATION_CLIENT_ID = "uk_oauth_application_client_id";
    static final String UK_OAUTH_APPLICATION_AUDIENCE = "uk_oauth_application_audience";

    static final String FK_OAUTH_ALLOWED_CLIENT_TARGET_APPLICATION_ID = "fk_oauth_allowed_client_target_application_id";
    static final String FK_NM_CLIENT_PERMISSIONS_RELATION_ID = "fk_nm_client_permissions_relation_id";
    static final String FK_NM_CLIENT_PERMISSIONS_PERMISSION_ID = "fk_nm_client_permissions_permission_id";

    static final String FK_NM_REQUIRED_PERMISSIONS_APP_ID = "fk_nm_required_permissions_app_id";
    static final String FK_NM_REQUIRED_PERMISSIONS_PERMISSION_ID = "fk_nm_required_permissions_permission_id";
    static final String FK_NM_REGISTERED_PERMISSIONS_APP_ID = "fk_nm_registered_permissions_app_id";
    static final String FK_NM_REGISTERED_PERMISSIONS_PERMISSION_ID = "fk_nm_registered_permissions_permission_id";
    static final String FK_OAUTH_CALLBACK_OAUTH_APPLICATION_ID = "fk_oauth_callback_oauth_application_id";
    static final String FK_OAUTH_ALLOWED_CLIENT_SELF_APPLICATION_ID = "fk_oauth_allowed_client_self_application_id";
    static final String UK_PERMISSION_NAME = "uk_permission_name";

    private DatabaseConstants() {}
}
