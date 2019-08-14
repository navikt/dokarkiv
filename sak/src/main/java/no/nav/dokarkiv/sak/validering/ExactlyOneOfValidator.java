package no.nav.dokarkiv.sak.validering;

import static no.nav.dokarkiv.sak.validering.CountFieldsMatching.count;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ONE;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ExactlyOneOfValidator implements ConstraintValidator<ExactlyOneOf, Object> {
	private String[] fields;

	@Override
	public void initialize(ExactlyOneOf exactlyOneOf) {
		this.fields = exactlyOneOf.fields();
	}

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		return Objects.equals(count(o, fields), LONG_ONE);
	}
}
