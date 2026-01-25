package hu.psprog.leaflet.lags.web.rest.controller;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationRegistrationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationResponse;
import hu.psprog.leaflet.lags.core.domain.response.OAuthApplicationSummaryResponse;
import hu.psprog.leaflet.lags.core.service.OAuthApplicationService;
import hu.psprog.leaflet.lags.web.security.Permit;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller endpoints for OAuth application definition operations.
 *
 * @author Peter Smith
 */
@Slf4j
@RestController
@RequestMapping("/access-management/oauth-applications")
public class ApplicationRegistrationController extends BaseManagementController {

    private final OAuthApplicationService oAuthApplicationService;

    @Autowired
    public ApplicationRegistrationController(OAuthApplicationService oAuthApplicationService) {
        this.oAuthApplicationService = oAuthApplicationService;
    }

    /**
     * Retrieves the details of the given OAuth application registration.
     *
     * @param applicationID application ID
     * @return OAuth application details
     */
    @Permit.Read.Applications
    @GetMapping("/{applicationID}")
    public OAuthApplicationResponse getOAuthApplication(@PathVariable UUID applicationID) {
        return oAuthApplicationService.getApplication(applicationID);
    }

    /**
     * Retrieves the given page of OAuth application registrations for listing (summary only).
     *
     * @param page 1-based page number, defaults to 0 (turns off pagination)
     * @return page of OAuth application definition summaries
     */
    @Permit.Read.Applications
    @GetMapping
    public Page<OAuthApplicationSummaryResponse> getOAuthApplications(@RequestParam(value = "page", defaultValue = "0") int page) {
        return oAuthApplicationService.getApplications(page);
    }

    /**
     * Creates a new OAuth application registration.
     *
     * @param request OAuth application data
     * @return registration response containing the generated ID and client secret
     */
    @Permit.Write.Applications
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthApplicationRegistrationResponse createApplication(@Valid @RequestBody OAuthApplicationRegistrationRequest request) {
        return oAuthApplicationService.createApplication(request);
    }

    /**
     * Edits an existing OAuth application registration.
     *
     * @param applicationID ID of the OAuth application to update
     * @param request OAuth application data
     * @return registration response containing the (original) generated ID
     */
    @Permit.Write.Applications
    @PutMapping("/{applicationID}")
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthApplicationRegistrationResponse editApplication(@PathVariable UUID applicationID, @Valid @RequestBody OAuthApplicationRegistrationRequest request) {
        return oAuthApplicationService.editApplication(applicationID, request);
    }

    /**
     * Regenerates the OAuth client secret of the given application.
     *
     * @param applicationID application ID
     * @return registration response containing the (original) generated ID and the new client secret
     */
    @Permit.Write.Applications
    @PutMapping("/{applicationID}/secret")
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthApplicationRegistrationResponse regenerateApplicationSecret(@PathVariable UUID applicationID) {
        return oAuthApplicationService.regenerateApplicationSecret(applicationID);
    }

    /**
     * Enables the given OAuth application.
     *
     * @param applicationID application ID
     * @return OAuth application details
     */
    @Permit.Write.Applications
    @PutMapping("/{applicationID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthApplicationResponse enableApplication(@PathVariable UUID applicationID) {
        return oAuthApplicationService.updateApplicationStatus(applicationID, true);
    }

    /**
     * Disables the given OAuth application.
     *
     * @param applicationID application ID
     * @return OAuth application details
     */
    @Permit.Write.Applications
    @DeleteMapping("/{applicationID}/status")
    @ResponseStatus(HttpStatus.CREATED)
    public OAuthApplicationResponse disableApplication(@PathVariable UUID applicationID) {
        return oAuthApplicationService.updateApplicationStatus(applicationID, false);
    }

    /**
     * Removes an existing Oauth application.
     *
     * @param applicationID application ID
     */
    @Permit.Write.Applications
    @DeleteMapping("/{applicationID}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(@PathVariable UUID applicationID) {
        oAuthApplicationService.deleteApplication(applicationID);
    }
}
