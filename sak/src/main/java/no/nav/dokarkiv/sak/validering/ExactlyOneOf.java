package no.nav.dokarkiv.sak.validering;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ExactlyOneOfValidator.class)
public @interface ExactlyOneOf {
	String message() default "{no.nav.sak.ExactlyOneOf}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String[] fields();

}
