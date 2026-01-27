package hu.psprog.leaflet.lags.core.domain.request.validation;

import hu.psprog.leaflet.lags.core.domain.request.OAuthApplicationRegistrationRequest;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for {@link OAuthApplicationRegistrationRequest.ResourceServerApplication} objects.
 *
 * @author Peter Smith
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResourceServerRequestValidator.class)
public @interface ValidResourceServerApplication {

    String message() default "Permissions of each allowed client must be a subset of registered permissions";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
