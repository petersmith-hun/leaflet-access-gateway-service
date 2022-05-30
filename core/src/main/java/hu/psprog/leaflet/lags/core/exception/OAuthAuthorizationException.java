package hu.psprog.leaflet.lags.core.exception;

import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;

/**
 * Generic OAuth authorization exception.
 *
 * @author Peter Smith
 */
public class OAuthAuthorizationException extends RuntimeException {

    private final OAuthErrorCode errorCode;

    /**
     * Creates an OAuth authorization exception with a message and ACCESS_DENIED OAuth error code.
     *
     * @param message exception message
     */
    public OAuthAuthorizationException(String message) {
        super(message);
        this.errorCode = OAuthErrorCode.ACCESS_DENIED;
    }

    /**
     * Creates an OAuth authorization exception with a message and the given OAuth error code.
     *
     * @param errorCode OAuth error code as {@link OAuthErrorCode} enum constant
     * @param message exception message
     */
    public OAuthAuthorizationException(OAuthErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the assigned OAuth error code.
     *
     * @return assigned OAuth error code as {@link OAuthErrorCode} enum constant
     */
    public OAuthErrorCode getErrorCode() {
        return errorCode;
    }
}
