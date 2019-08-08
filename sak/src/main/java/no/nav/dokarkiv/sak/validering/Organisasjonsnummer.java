package no.nav.dokarkiv.sak.validering;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = OrganisasjonsnummerValidator.class)
public @interface Organisasjonsnummer {
	String message() default "{no.nav.sak.Organisasjonsnummer.ugyldig}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
