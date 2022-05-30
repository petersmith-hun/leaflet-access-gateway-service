package hu.psprog.leaflet.lags.core.domain.response;

import org.springframework.http.HttpStatus;

/**
 * Possible OAuth error codes.
 *
 * @author Peter Smith
 */
public enum OAuthErrorCode {

    ACCESS_DENIED("access_denied", HttpStatus.BAD_REQUEST),
    INVALID_CLIENT("invalid_client", HttpStatus.UNAUTHORIZED),
    INVALID_GRANT("invalid_grant", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("invalid_request", HttpStatus.BAD_REQUEST),
    INVALID_SCOPE("invalid_scope", HttpStatus.BAD_REQUEST),
    SERVER_ERROR("server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    UNAUTHORIZED_CLIENT("unauthorized_client", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_GRANT_TYPE("unsupported_grant_type", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final HttpStatus mappedStatus;

    OAuthErrorCode(String errorCode, HttpStatus mappedStatus) {
        this.errorCode = errorCode;
        this.mappedStatus = mappedStatus;
    }

    /**
     * Returns the OAuth specification compatible error code.
     *
     * @return the OAuth specification compatible error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the mapped HTTP status code of the given OAuth error code.
     *
     * @return the mapped HTTP status code
     */
    public HttpStatus getMappedStatus() {
        return mappedStatus;
    }
}
