package hu.psprog.leaflet.lags.core.domain.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import hu.psprog.leaflet.lags.core.domain.internal.OAuthConstants;
import lombok.Builder;

import java.util.Date;

/**
 * Domain class representing the results of a token introspection request.
 *
 * @author Peter Smith
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TokenIntrospectionResult(
        boolean active,
        String username,
        @JsonProperty(OAuthConstants.Request.CLIENT_ID) String clientID,
        @JsonProperty(OAuthConstants.Token.EXPIRATION) Date expiration) {

    public static final TokenIntrospectionResult FAILED_INTROSPECTION_RESULT = TokenIntrospectionResult.builder()
            .active(false)
            .build();

}
