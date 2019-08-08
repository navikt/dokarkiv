package no.nav.dokarkiv.sak.validering;

import static no.nav.dokarkiv.sak.validering.CountFieldsMatching.count;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class AllorNoneOfValidator implements ConstraintValidator<AllOrNoneOf, Object> {
	private String[] fields;

	@Override
	public void initialize(AllOrNoneOf allOrNoneOf) {
		this.fields = allOrNoneOf.fields();
	}

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		Long countWithValue = count(o, fields);
		return countWithValue == 0 || countWithValue == fields.length;
	}
}
