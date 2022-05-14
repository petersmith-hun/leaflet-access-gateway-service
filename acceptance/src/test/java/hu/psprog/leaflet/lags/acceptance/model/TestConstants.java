package hu.psprog.leaflet.lags.acceptance.model;

/**
 * Constants used in acceptance tests.
 *
 * @author Peter Smith
 */
public interface TestConstants {

    /**
     * Attribute (generic) parameters.
     */
    enum Attribute implements TestConstants {

        AUDIENCE("audience"),
        CLIENT_ID("client_id"),
        CLIENT_SECRET("client_secret"),
        CODE("code"),
        EMAIL("email"),
        GRANT_TYPE("grant_type"),
        LOCATION("location"),
        PASSWORD("password"),
        PASSWORD_CONFIRM("passwordConfirmation"),
        RECAPTCHA_TOKEN("recaptchaToken"),
        REDIRECT_URI("redirect_uri"),
        RESPONSE_ENTITY("response_entity"),
        RESPONSE_TYPE("response_type"),
        SCOPE("scope"),
        STATE("state"),
        TOKEN("token"),
        USER_AUTH("user_auth"),
        USERNAME("username"),

        PASSWORD_RESET_CONFIRMATION_MAIL("mail_passwordResetConfirmation"),
        PASSWORD_RESET_REQUEST_MAIL("mail_passwordResetRequest"),
        SIGN_UP_CONFIRMATION_MAIL("mail_signUpConfirmation");

        private final String value;

        Attribute(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Flag (boolean) parameters.
     */
    enum Flag implements TestConstants {

        USE_AUTHORIZATION_CODE("useAuthorizationCode"),
        USE_RECAPTCHA_VERIFICATION("useReCaptchaVerification"),
        USE_REDIRECT_URI("useRedirectURI");

        private final String value;

        Flag(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Header parameters.
     */
    enum Header implements TestConstants {

        AUTHORIZATION("Authorization"),
        LOCATION("Location");

        private final String value;

        Header(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    /**
     * Returns the associated value.
     *
     * @return the associated value
     */
    String getValue();
}
