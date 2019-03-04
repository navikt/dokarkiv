package no.nav.dokarkiv.journalpost.v1.rjoark201.util;

import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import org.apache.commons.lang3.StringUtils;

public final class RequestUtils {

	private RequestUtils(){
		//no-op
	}

	public static void validateId(String journalpostId, String feltnavn) {
		try {
			hasText(journalpostId, feltnavn);
			convertStringToLong(journalpostId, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new UgyldigInputException(String.format("%s. journalpostId=%s", e.getMessage(), journalpostId));
		}
	}

	public static void validateJournalfEnhet(String journalfEnhet, String feltnavn){
		try {
			hasText(journalfEnhet, feltnavn);
			hasLength(journalfEnhet, feltnavn, 4);
			isNumeric(journalfEnhet, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new UgyldigInputException(String.format("%s. journalfEnhet=%s", e.getMessage(), journalfEnhet));
		}
	}

	private static void convertStringToLong(String input, String feltnavn) {
		try {
			Long.parseLong(input);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("%s er ikke et tall", feltnavn));
		}
	}

	private static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalArgumentException(String.format("%s kan ikke være null eller tom", feltnavn));
		}
	}

	private static void hasLength(String input, String feltnavn, int length) {
		if (input.length() != length) {
			throw new IllegalArgumentException(String.format("%s skal være av %d", feltnavn, length));
		}
	}

	private static void isNumeric(String input, String feltnavn) {
		if (! StringUtils.isNumeric(input)) {
			throw new IllegalArgumentException(String.format("%s skal være numerisk", feltnavn));
		}
	}
}
