package hu.psprog.leaflet.lags.core.domain.internal;

/**
 * OAuth2 specification compatible request and token constants.
 *
 * @author Peter Smith
 */
public interface OAuthConstants {

    /**
     * Constants used in OAuth2 authorization and token requests.
     */
    interface Request {
        String GRANT_TYPE = "grant_type";
        String CLIENT_ID = "client_id";
        String USERNAME = "username";
        String PASSWORD = "password";
        String AUDIENCE = "audience";
        String SCOPE = "scope";
        String CODE = "code";
        String RESPONSE_TYPE = "response_type";
        String REDIRECT_URI = "redirect_uri";
        String STATE = "state";
    }

    /**
     * JWT token claim constants.
     */
    interface Token {
        String JTI = "jti";
        String SCOPE = "scope";
        String SUBJECT = "sub";
        String USER = "usr";
        String ROLE = "rol";
        String NAME = "name";
        String USER_ID = "uid";
        String EXPIRATION = "exp";
        String AUDIENCE = "aud";
        String ISSUED_AT = "iat";
        String ISSUER = "iss";
        String NOT_BEFORE = "nbf";
    }
}
