package no.nav.dokarkiv.oppdaterjournalpost.v1.util;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Utils {

	public static void validateId(String journalpostId, String feltnavn) {
		try {
			hasText(journalpostId, feltnavn);
			convertStringToLong(journalpostId, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalpostId=%s", e.getMessage(), journalpostId));
		}
	}

	public static Long convertStringToLong(String input, String feltnavn) {
		try {
			return Long.parseLong(input);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("%s er ikke et tall", feltnavn));
		}
	}

	public static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalArgumentException(String.format("%s kan ikke være null eller tom", feltnavn));
		}
	}

	public static void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

	public static void assertNotNull(Object object, String fieldName) throws IllegalArgumentException {
		if (object == null) {
			throw new InputValideringFeiletException(String.format("%s kan ikke være null", fieldName));
		}
	}
}
