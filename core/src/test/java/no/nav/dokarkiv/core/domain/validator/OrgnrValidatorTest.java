package no.nav.dokarkiv.core.domain.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.exceptions.InvalidOrgnrException;
import org.junit.Test;

/**
 * Unit tests for OrgnrValidator.
 *
 * @author Hans Olav Loftum, BEKK
 */
public class OrgnrValidatorTest {

	@Test
	public void shouldValidateValidOrgnr() {
		String orgnr = "123456785";
		assertOrgnrIsValid(orgnr);
	}

	@Test
	public void shouldValidateValidOrgnrEndingWith0() {
		String orgnr = "974773880";
		assertOrgnrIsValid(orgnr);
	}

	@Test
	public void shouldValidateValidOrgnrWithWhitespaces() {
		String orgnr = "123 456 785";
		assertOrgnrIsValid(orgnr);
	}

	@Test
	public void validationShouldFailWhenOrgnrContainsOtherThanNumbers() {
		String orgnr = "¤23456785";
		assertOrgnrIsInvalid(orgnr, "Validation should fail when orgnr contains characters");
	}

	@Test
	public void validationShouldFailWhenControlNumberIsTen() {
		String orgnr = "10305678-";
		assertOrgnrIsInvalid(orgnr, "Validation should fail when control number is 10");
	}

	@Test
	public void validationShouldFailWhenOrgnrIsTooShort() {
		String orgnr = "12345678";
		assertOrgnrIsInvalid(orgnr, "Validation should fail when orgnr is too short");
	}

	@Test
	public void validationShouldFailWhenOrgnrIsTooLong() {
		String orgnr = "1234567852";
		assertOrgnrIsInvalid(orgnr, "Validation should fail when orgnr is too long");
	}

	@Test
	public void validationShouldFailWhenControlNumberIsIncorrect() {
		String orgnr = "123456786";
		assertOrgnrIsInvalid(orgnr, "Validation should fail when control number is incorrect");
	}
	
	@Test
	public void shouldReturnIsValidTrue() {
		String orgnr = "123456785";
		assertThat(OrgnrValidator.isOrgnr(orgnr), is(true));
	}
	
	@Test
	public void shouldReturnIsValidFalse() {
		String orgnr = "12345678";
		assertThat(OrgnrValidator.isOrgnr(orgnr), is(false));
	}

	private void assertOrgnrIsInvalid(String orgnr, String errorMessage) {
		try {
			OrgnrValidator.validate(orgnr);
			fail(errorMessage);
		} catch (InvalidOrgnrException e) {

		}
	}

	private void assertOrgnrIsValid(String orgnr) throws InvalidOrgnrException {
		OrgnrValidator.validate(orgnr);
	}
}
