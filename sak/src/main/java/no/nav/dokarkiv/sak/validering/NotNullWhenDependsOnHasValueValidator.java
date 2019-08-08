package no.nav.dokarkiv.sak.validering;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullWhenDependsOnHasValueValidator implements ConstraintValidator<NotNullWhenDependsOnHasValue, Object> {
	private String field;
	private String dependsOnField;

	@Override
	public void initialize(NotNullWhenDependsOnHasValue notNullWhenDependsOnHasValue) {
		this.field = notNullWhenDependsOnHasValue.field();
		this.dependsOnField = notNullWhenDependsOnHasValue.dependsOnField();
	}

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		try {
			String fieldValue = BeanUtils.getProperty(o, field);
			String dependFieldValue = BeanUtils.getProperty(o, dependsOnField);
			if (StringUtils.isBlank(dependFieldValue)) {
				return true;
			}
			return StringUtils.isNotBlank(fieldValue);

		} catch (Exception e) {
			throw new IllegalStateException("Kunne ikke lese fields", e);
		}
	}
}
