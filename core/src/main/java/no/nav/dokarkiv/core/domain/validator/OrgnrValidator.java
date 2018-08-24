package no.nav.dokarkiv.core.domain.validator;

import no.nav.dokarkiv.core.exceptions.InvalidOrgnrException;
import org.apache.commons.lang3.StringUtils;

/**
 * Validates organization numbers.
 * 
 * @author Hans Olav Loftum, BEKK
 */
public final class OrgnrValidator {

	private static final int[] WEIGHTS = { 3, 2, 7, 6, 5, 4, 3, 2 };

	/**
	 * Avoid instantiation.
	 */
	private OrgnrValidator() {
	}
	
	/**
	 * Validates an organization number by length, type of characters and
	 * modulus 11.
	 * 
	 * @param orgnr
	 *            the organizatino number to validate.
	 */
	public static void validate(String orgnr) {
		String value = StringUtils.deleteWhitespace(orgnr);
		verifyOrnrHasCorrectLength(value);
		verifyOrgnrOnlyContainsNumbers(value);
		verifyOrgnrIsMod11Compliant(value);
	}
	
	public static boolean isOrgnr(String orgnr) {
		try {
			validate(orgnr);
			return true;
		} catch (InvalidOrgnrException e) {
			return false;
		}
	}

	private static void verifyOrnrHasCorrectLength(String value) {
		int length = value.length();
		if (length != 9) {
			throw new InvalidOrgnrException("Orgnr should have length=9. Actual=" + length + " Actual orgnr=" + value);
		}
	}

	private static void verifyOrgnrOnlyContainsNumbers(String value) {
		try {
			Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidOrgnrException("Orgnr should only contain numbers. Actual orgnr=" + value, e);
		}
	}

	private static void verifyOrgnrIsMod11Compliant(String value) {
		int[] fields = getFields(value);
		int actualControlNumber = getControlNumber(value);
		int expectedControlNumber = getExpectedControlNumber(fields);
		if (!isValidControlNumber(expectedControlNumber) || (actualControlNumber != expectedControlNumber)) {
			throw new InvalidOrgnrException("Orgnr is not mod 11 compliant. Actual orgnr=" + value);
		}
	}

	private static boolean isValidControlNumber(int controlNumber) {
		return controlNumber != 10;
	}

	private static int getExpectedControlNumber(int... fields) {
		int productSum = getProductSum(fields);
		int rest = productSum % 11;
		return rest == 0 ? 0 : 11 - rest;
	}

	private static int getProductSum(int... fields) {
		int productSum = 0;
		for (int ii = 0; ii < fields.length; ii++) {
			productSum += fields[ii] * WEIGHTS[ii];
		}
		return productSum;
	}

	private static int getControlNumber(String value) {
		return Integer.parseInt(value.substring(8));
	}

	private static int[] getFields(String value) {
		int[] fields = new int[8];
		for (int ii = 0; ii < 8; ii++) {
			fields[ii] = Integer.parseInt(value.substring(ii, ii + 1));
		}
		return fields;
	}
}
