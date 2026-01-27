package hu.psprog.leaflet.lags.core.domain.response;

import lombok.Builder;

import java.util.UUID;

/**
 * Response model containing the generated client secret on a successful application registration.
 *
 * @param id generated internal ID of the definition
 * @param clientSecret generated client secret for the definition
 */
@Builder
public record OAuthApplicationRegistrationResponse(
        UUID id,
        String clientSecret
) { }
