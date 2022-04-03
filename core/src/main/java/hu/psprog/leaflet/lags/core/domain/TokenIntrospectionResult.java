package hu.psprog.leaflet.lags.core.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Domain class representing the results of a token introspection request.
 *
 * @author Peter Smith
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TokenIntrospectionResult {

    public static final TokenIntrospectionResult FAILED_INTROSPECTION_RESULT = TokenIntrospectionResult.builder()
            .active(false)
            .build();

    private final boolean active;
    private final String username;

    @JsonProperty(OAuthConstants.Request.CLIENT_ID)
    private final String clientID;

    @JsonProperty(OAuthConstants.Token.EXPIRATION)
    private final Date expiration;
}
