package no.nav.dokarkiv.sak.validering;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotNullWhenDependsOnHasValueValidator.class)
@Documented
public @interface NotNullWhenDependsOnHasValue {
	String field();

	String dependsOnField();

	String message() default "{no.nav.sak.NotNullWhenDependsOnHasValue}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
