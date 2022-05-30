package hu.psprog.leaflet.lags.core.domain.request;

import hu.psprog.leaflet.lags.core.domain.response.OAuthErrorCode;
import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

import java.util.stream.Stream;

/**
 * Available OAuth2 authorization response types for authorization code flow.
 *
 * @author Peter Smith
 */
public enum AuthorizationResponseType {

    CODE("code");

    private final String responseTypeName;

    AuthorizationResponseType(String responseTypeName) {
        this.responseTypeName = responseTypeName;
    }

    /**
     * Returns the OAuth2 specification compatible response type name.
     *
     * @return OAuth2 specification compatible response type name
     */
    public String getResponseTypeName() {
        return responseTypeName;
    }

    /**
     * Parses the response type based on the provided OAuth2 specification compatible name.
     *
     * @param responseTypeName OAuth2 specification compatible response type name
     * @return parsed response type
     * @throws OAuthAuthorizationException if provided grant name is not registered (unsupported or invalid)
     */
    public static AuthorizationResponseType parseResponseType(String responseTypeName) {

        return Stream.of(values())
                .filter(responseType -> responseType.getResponseTypeName().equals(responseTypeName))
                .findFirst()
                .orElseThrow(() -> new OAuthAuthorizationException(OAuthErrorCode.INVALID_REQUEST, String.format("Unsupported response type [%s]", responseTypeName)));
    }
}
