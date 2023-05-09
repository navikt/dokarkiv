package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;

import static java.lang.String.format;

public final class CommonValidator {

	private CommonValidator() {
		//no-op
	}

	public static void validateId(String id, String feltnavn) {
		try {
			hasText(id, feltnavn);
			isNumeric(id, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s. %s=%s", e.getMessage(), feltnavn, id));
		}
	}

	public static void validateBoolean(Boolean value, String feltnavn) {
		if (value == null) {
			throw new InputValideringFeiletException(format("Feltet %s kan ikke være null eller tomt", feltnavn));
		}
	}

	public static void validateJournalfoerendeEnhet(String journalfoerendeEnhet, String feltnavn) {
		try {
			hasText(journalfoerendeEnhet, feltnavn);
			hasLength(journalfoerendeEnhet, feltnavn, 4);
			isNumeric(journalfoerendeEnhet, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s. journalfoerendeEnhet=%s", e.getMessage(), journalfoerendeEnhet));
		}
	}

    public static void hasText(String input, String feltnavn) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException(format("Feltet %s kan ikke være null eller tomt", feltnavn));
        }
    }

	private static void hasLength(String input, String feltnavn, int length) {
		if (input.length() != length) {
			throw new IllegalArgumentException(format("Feltet %s må ha lengde=%d, men har lengde=%s", feltnavn, length, input.length()));
		}
	}

	private static void isNumeric(String input, String feltnavn) {
		if (!StringUtils.isNumeric(input)) {
			throw new IllegalArgumentException(format("Feltet %s må være et heltall. Mottatt verdi=%s", feltnavn, input));
		}
	}

	public static void validateNotNull(Object o, String feltnavn) {
		validateNotNull(o, feltnavn, null);
	}

	public static void validateNotNull(Object o, String feltnavn, String ekstraInformasjon) {
		if (o == null) {
			throw new InputValideringFeiletException(feltnavn + " kan ikke være null" + (ekstraInformasjon != null ? ", " + ekstraInformasjon : "" ) + "!" );
		}
	}
}
