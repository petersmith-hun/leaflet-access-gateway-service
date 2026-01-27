package hu.psprog.leaflet.lags.core.domain.request.validation;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

/**
 * Executes the following global validations on an {@link OAuthApplicationRegistrationRequest} instance:
 * <ul>
 *     <li>An application with {@link OAuthApplicationRegistrationRequest.RegistrationType#CLIENT} have a "client" setting segment.</li>
 *     <li>An application with {@link OAuthApplicationRegistrationRequest.RegistrationType#RESOURCE_SERVER} have a "resource server" setting segment.</li>
 *     <li>An application with {@link OAuthApplicationRegistrationRequest.RegistrationType#MIDDLE_RESOURCE_SERVER} have both of above.</li>
 * </ul>
 *
 * @author Peter Smith
 */
public class ApplicationRegistrationRequestValidator implements ConstraintValidator<ValidApplicationRegistrationRequest, OAuthApplicationRegistrationRequest> {

    @Override
    public boolean isValid(OAuthApplicationRegistrationRequest request, ConstraintValidatorContext context) {

        return isClient(request)
                || isResourceServer(request)
                || isMiddleResourceServer(request);
    }

    private boolean isClient(OAuthApplicationRegistrationRequest request) {

        return isTypeSelected(request, OAuthApplicationRegistrationRequest.RegistrationType.CLIENT)
                && isClientSegmentPopulated(request)
                && !isResourceServerSegmentPopulated(request);
    }

    private boolean isResourceServer(OAuthApplicationRegistrationRequest request) {

        return isTypeSelected(request, OAuthApplicationRegistrationRequest.RegistrationType.RESOURCE_SERVER)
                && !isClientSegmentPopulated(request)
                && isResourceServerSegmentPopulated(request);
    }

    private boolean isMiddleResourceServer(OAuthApplicationRegistrationRequest request) {

        return isTypeSelected(request, OAuthApplicationRegistrationRequest.RegistrationType.MIDDLE_RESOURCE_SERVER)
                && isClientSegmentPopulated(request)
                && isResourceServerSegmentPopulated(request);
    }

    private boolean isTypeSelected(OAuthApplicationRegistrationRequest request, OAuthApplicationRegistrationRequest.RegistrationType type) {
        return request.registrationType() == type;
    }

    private boolean isClientSegmentPopulated(OAuthApplicationRegistrationRequest request) {
        return Objects.nonNull(request.client());
    }

    private boolean isResourceServerSegmentPopulated(OAuthApplicationRegistrationRequest request) {
        return Objects.nonNull(request.resourceServer());
    }
}
