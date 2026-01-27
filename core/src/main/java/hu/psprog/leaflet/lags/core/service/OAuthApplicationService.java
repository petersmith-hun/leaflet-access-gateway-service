package hu.psprog.leaflet.lags.core.service;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationRegistrationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * OAuth application definition operations.
 *
 * @author Peter Smith
 */
public interface OAuthApplicationService {

    /**
     * Creates a new OAuth application registration.
     *
     * @param request OAuth application data
     * @return registration response containing the generated ID and client secret
     */
    OAuthApplicationRegistrationResponse createApplication(OAuthApplicationRegistrationRequest request);

    /**
     * Edits an existing OAuth application registration.
     *
     * @param applicationID ID of the OAuth application to update
     * @param request OAuth application data
     * @return registration response containing the (original) generated ID
     */
    OAuthApplicationRegistrationResponse editApplication(UUID applicationID, OAuthApplicationRegistrationRequest request);

    /**
     * Retrieves the details of the given OAuth application registration.
     *
     * @param applicationID application ID
     * @return OAuth application details
     */
    OAuthApplicationResponse getApplication(UUID applicationID);

    /**
     * Retrieves the given page of OAuth application registrations for listing (summary only).
     *
     * @param page 1-based page number
     * @return page of OAuth application definition summaries
     */
    Page<OAuthApplicationSummaryResponse> getApplications(int page);

    /**
     * Enables/disables the given OAuth application.
     *
     * @param applicationID application ID
     * @param enabled target status
     * @return OAuth application details
     */
    OAuthApplicationResponse updateApplicationStatus(UUID applicationID, boolean enabled);

    /**
     * Regenerates the OAuth client secret of the given application.
     *
     * @param applicationID application ID
     * @return registration response containing the (original) generated ID and the new client secret
     */
    OAuthApplicationRegistrationResponse regenerateApplicationSecret(UUID applicationID);

    /**
     * Removes an existing Oauth application.
     *
     * @param applicationID application ID
     */
    void deleteApplication(UUID applicationID);
}
