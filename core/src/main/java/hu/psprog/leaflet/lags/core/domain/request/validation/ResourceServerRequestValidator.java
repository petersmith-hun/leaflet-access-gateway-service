package hu.psprog.leaflet.lags.core.domain.request.validation;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Executes the following global validation on an {@link OAuthApplicationRegistrationRequest.ResourceServerApplication} instance:
 * <ul>
 *     <li>Permissions of a resource server application's allowed clients must be subsets of the application's registered permissions.</li>
 * </ul>
 *
 * @author Peter Smith
 */
public class ResourceServerRequestValidator implements ConstraintValidator<ValidResourceServerApplication, OAuthApplicationRegistrationRequest.ResourceServerApplication> {

    @Override
    public boolean isValid(OAuthApplicationRegistrationRequest.ResourceServerApplication request, ConstraintValidatorContext context) {

        Set<UUID> registeredPermissions = new HashSet<>(request.registeredPermissions());

        return request.allowedClients()
                .stream()
                .map(OAuthApplicationRegistrationRequest.ResourceServerApplication.AllowedClient::allowedPermissions)
                .allMatch(registeredPermissions::containsAll);
    }
}
