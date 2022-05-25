package hu.psprog.leaflet.lags.core.domain.request;

/**
 * Interface for OAuth authorization related request models.
 *
 * @author Peter Smith
 */
public interface OAuthRequest {

    /**
     * Returns the grant type specified by the request.
     *
     * @return the grant type specified by the request
     */
    GrantType getGrantType();

    /**
     * Returns the source client ID specified by the request.
     *
     * @return the source client ID specified by the request
     */
    String getClientID();
}
