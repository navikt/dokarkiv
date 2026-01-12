package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.UgyldigMediaTypeException;

import java.util.Set;

import static no.nav.dokarkiv.journalpost.v1.controllers.SettBrevdataController.VARIANT_FORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.controllers.SettBrevdataController.VARIANT_FORMAT_PRODUKSJON;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

public class SettBrevdataValidator {

	public static final String APPLICATION_RTF = "application/rtf";
	private static final Set<String> GYLDIGE_CONTENT_TYPES = Set.of(APPLICATION_RTF, APPLICATION_PDF_VALUE);
	private static final Set<String> GYLDIGE_VARIANT_FORMAT = Set.of(VARIANT_FORMAT_PRODUKSJON, VARIANT_FORMAT_ARKIV);

	public static void validateRequest(String contentType, String variantFormat, byte[] brevdata) {
		if (!GYLDIGE_CONTENT_TYPES.contains(contentType)) {
			throw new UgyldigMediaTypeException("Content-Type header må være en av " + GYLDIGE_CONTENT_TYPES);
		}

		if (!GYLDIGE_VARIANT_FORMAT.contains(variantFormat)) {
			throw new InputValideringFeiletException("variantFormat må være en av " + GYLDIGE_VARIANT_FORMAT);
		}

		if (brevdata.length == 0) {
			throw new InputValideringFeiletException("brevdata må ha lengde mer enn 0");
		}

		if (APPLICATION_RTF.equals(contentType) && !variantFormat.equals(VARIANT_FORMAT_PRODUKSJON)) {
			throw new InputValideringFeiletException("Hvis contentType er application/rtf så må variantFormat være PRODUKSJON");
		}

		if (APPLICATION_PDF_VALUE.equals(contentType) && !variantFormat.equals(VARIANT_FORMAT_ARKIV)) {
			throw new InputValideringFeiletException("Hvis contentType er application/pdf så må variantFormat være ARKIV");
		}

	}
}
