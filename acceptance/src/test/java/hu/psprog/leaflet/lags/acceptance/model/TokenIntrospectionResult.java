package hu.psprog.leaflet.lags.acceptance.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.psprog.leaflet.lags.core.domain.OAuthConstants;
import lombok.Data;

import java.util.Date;

/**
 * Domain class representing the results of a token introspection request.
 *
 * @author Peter Smith
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TokenIntrospectionResult {

    private boolean active;
    private String username;

    @JsonProperty(OAuthConstants.Request.CLIENT_ID)
    private String clientID;

    @JsonProperty(OAuthConstants.Token.EXPIRATION)
    private Date expiration;
}
