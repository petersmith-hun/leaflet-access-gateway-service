package hu.psprog.leaflet.lags.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for OAuth token request errors.
 *
 * @author Peter Smith
 */
public record TokenRequestError(
        @JsonProperty("error") String errorCode,
        @JsonProperty("error_description") String errorDescription
) { }
