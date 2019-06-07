package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;

public final class CommonValidator {

	private CommonValidator() {
		//no-op
	}

	public static void validateId(String id, String feltnavn) {
		try {
			hasText(id, feltnavn);
			convertStringToLong(id, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. %s=%s", e.getMessage(), feltnavn, id));
		}
	}

	public static void validateJournalfoerendeEnhet(String journalfoerendeEnhet, String feltnavn) {
		try {
			hasText(journalfoerendeEnhet, feltnavn);
			hasLength(journalfoerendeEnhet, feltnavn, 4);
			isNumeric(journalfoerendeEnhet, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalfoerendeEnhet=%s", e.getMessage(), journalfoerendeEnhet));
		}
	}

    public static void hasText(String input, String feltnavn) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException(String.format("%s kan ikke være null eller tom", feltnavn));
        }
    }

	private static void convertStringToLong(String input, String feltnavn) {
		try {
			Long.parseLong(input);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("%s er ikke et tall", feltnavn));
		}
	}

	private static void hasLength(String input, String feltnavn, int length) {
		if (input.length() != length) {
			throw new IllegalArgumentException(String.format("%s skal være av %d", feltnavn, length));
		}
	}

	private static void isNumeric(String input, String feltnavn) {
		if (!StringUtils.isNumeric(input)) {
			throw new IllegalArgumentException(String.format("%s skal være numerisk", feltnavn));
		}
	}
}
