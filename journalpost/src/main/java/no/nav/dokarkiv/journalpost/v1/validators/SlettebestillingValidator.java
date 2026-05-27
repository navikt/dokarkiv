package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.UgyldigSlettebestillingException;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;

import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.POL;
import static no.nav.dokarkiv.core.domain.entities.Slettebestilling.SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class SlettebestillingValidator {

	public static void validerSlettebestilling(SlettebestillingRequest slettebestilling) {
		if (!(ARK.name().equals(slettebestilling.hjemmel()) || POL.name().equals(slettebestilling.hjemmel()))) {
			throw new UgyldigSlettebestillingException("Hjemmel må være enten ARK eller POL");
		}
		if (isBlank(slettebestilling.begrunnelse())) {
			throw new UgyldigSlettebestillingException("Begrunnelse må oppgis og kan ikke være blank!");
		}
		if (slettebestilling.begrunnelse() != null && slettebestilling.begrunnelse().length() > SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH) {
			throw new UgyldigSlettebestillingException("Begrunnelse er for lang! Feltet kan maksimalt være %d tegn lang, var %d tegn lang"
					.formatted(SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH, slettebestilling.begrunnelse().length()));
		}
	}
}
