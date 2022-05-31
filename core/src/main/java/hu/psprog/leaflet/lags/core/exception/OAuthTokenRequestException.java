package hu.psprog.leaflet.lags.core.exception;

import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;

/**
 * Generic OAuth token request exception.
 *
 * @author Peter Smith
 */
public class OAuthTokenRequestException extends OAuthAuthorizationException {

    /**
     * Creates an OAuth token request exception with a message and the given OAuth error code.
     *
     * @param errorCode OAuth error code as {@link OAuthErrorCode} enum constant
     * @param message exception message
     */
    public OAuthTokenRequestException(OAuthErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
