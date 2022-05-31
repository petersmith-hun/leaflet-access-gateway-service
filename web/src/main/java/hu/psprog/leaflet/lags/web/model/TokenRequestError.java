package hu.psprog.leaflet.lags.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response model for OAuth token request errors.
 *
 * @author Peter Smith
 */
@Data
@AllArgsConstructor
public class TokenRequestError {

    @JsonProperty("error")
    private final String errorCode;

    @JsonProperty("error_description")
    private final String errorDescription;
}
