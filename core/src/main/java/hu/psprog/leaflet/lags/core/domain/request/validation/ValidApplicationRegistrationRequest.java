package hu.psprog.leaflet.lags.core.domain.request.validation;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for {@link OAuthApplicationRegistrationRequest} objects.
 *
 * @see ApplicationRegistrationRequestValidator
 * @author Peter Smith
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ApplicationRegistrationRequestValidator.class)
public @interface ValidApplicationRegistrationRequest {

    String message() default "Presence of client and resource server configuration segments must match the selected registration type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
