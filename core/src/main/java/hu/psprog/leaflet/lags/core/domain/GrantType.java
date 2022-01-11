package hu.psprog.leaflet.lags.core.domain;

import hu.psprog.leaflet.lags.core.exception.OAuthAuthorizationException;

import java.util.stream.Stream;

/**
 * Supported OAuth2 grant flow types.
 *
 * @author Peter Smith
 */
public enum GrantType {

    AUTHORIZATION_CODE("code"),
    CLIENT_CREDENTIALS("client_credentials"),
    PASSWORD("password");

    private final String grantTypeName;

    GrantType(String grantTypeName) {
        this.grantTypeName = grantTypeName;
    }

    /**
     * Returns the OAuth2 specification compatible grant type name.
     *
     * @return OAuth2 specification compatible grant type name
     */
    public String getGrantTypeName() {
        return grantTypeName;
    }

    /**
     * Parses the grant type based on the provided OAuth2 specification compatible name.
     *
     * @param grantTypeName OAuth2 specification compatible grant name
     * @return parsed grant type
     * @throws OAuthAuthorizationException if provided grant name is not registered (unsupported or invalid)
     */
    public static GrantType parseGrantType(String grantTypeName) {

        return Stream.of(values())
                .filter(grantType -> grantType.getGrantTypeName().equals(grantTypeName))
                .findFirst()
                .orElseThrow(() -> new OAuthAuthorizationException(String.format("Unsupported grant type [%s]", grantTypeName)));
    }
}
