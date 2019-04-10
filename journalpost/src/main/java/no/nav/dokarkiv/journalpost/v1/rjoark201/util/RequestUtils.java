package no.nav.dokarkiv.journalpost.v1.rjoark201.util;

import static no.nav.dokarkiv.journalpost.v1.rjoark201.util.AvvikstypeConstants.AVBRYT;
import static no.nav.dokarkiv.journalpost.v1.rjoark201.util.AvvikstypeConstants.FEILREGISTRER_SAKSRELASJON;
import static no.nav.dokarkiv.journalpost.v1.rjoark201.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERING;
import static no.nav.dokarkiv.journalpost.v1.rjoark201.util.AvvikstypeConstants.UKJENT_BRUKER;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public final class RequestUtils {

	private RequestUtils() {
		//no-op
	}

	public static void validateId(String journalpostId, String feltnavn) {
		try {
			hasText(journalpostId, feltnavn);
			convertStringToLong(journalpostId, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalpostId=%s", e.getMessage(), journalpostId));
		}
	}

	public static void validateAvvikstype(String avvikstype) {
		List<String> validAvvikstyper = Arrays.asList(FEILREGISTRER_SAKSRELASJON, OPPHEV_FEILREGISTRERING, UKJENT_BRUKER, AVBRYT);
		if (!validAvvikstyper.contains(avvikstype)) {
			throw new InputValideringFeiletException(String.format("Ugyldig avvikstype"));
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
		if (!StringUtils.isNumeric(input)) {
			throw new IllegalArgumentException(String.format("%s skal være numerisk", feltnavn));
		}
	}
}
