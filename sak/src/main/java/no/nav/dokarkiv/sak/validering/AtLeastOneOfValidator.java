package no.nav.dokarkiv.sak.validering;

import static no.nav.dokarkiv.sak.validering.CountFieldsMatching.count;
import static org.apache.commons.lang3.math.NumberUtils.LONG_ONE;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AtLeastOneOfValidator implements ConstraintValidator<AtLeastOneOf, Object> {
	private String[] fields;

	@Override
	public void initialize(AtLeastOneOf atLeastOneOf) {
		this.fields = atLeastOneOf.fields();
	}

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		return count(o, fields) >= LONG_ONE;
	}
}
