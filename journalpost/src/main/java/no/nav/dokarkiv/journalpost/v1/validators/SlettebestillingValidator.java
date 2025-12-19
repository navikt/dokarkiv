package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.UgyldigSlettebestillingException;
import no.nav.dokarkiv.journalpost.v1.api.SlettebestillingRequest;

import static no.nav.dokarkiv.core.domain.codes.SlettebestillingArsakCode.ENKELTSLETTING;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.ARK;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingHjemmelCode.POL;
import static no.nav.dokarkiv.core.domain.codes.SlettebestillingTypeCode.DOKUMENT;
import static no.nav.dokarkiv.core.domain.entities.Slettebestilling.SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH;

public class SlettebestillingValidator {

	public static void validerSlettebestilling(SlettebestillingRequest slettebestilling) {
		if (!DOKUMENT.name().equals(slettebestilling.slettebestillingType())) {
			throw new UgyldigSlettebestillingException(slettebestilling.slettebestillingType() + " er ikke en gyldig verdi for slettebestillingType");
		}
		if (!ENKELTSLETTING.name().equals(slettebestilling.arsak())) {
			throw new UgyldigSlettebestillingException("Årsak må være ENKELTSLETTING når slettebestillingType er DOKUMENT");
		}
		if (slettebestilling.dokumentInfoId() == null) {
			throw new UgyldigSlettebestillingException("DokumentInfoId kan ikke være null når slettebestillingType er DOKUMENT");
		}
		if (!(ARK.name().equals(slettebestilling.hjemmel()) || POL.name().equals(slettebestilling.hjemmel()))) {
			throw new UgyldigSlettebestillingException("Hjemmel må være enten ARK eller POL");
		}
		if (slettebestilling.begrunnelse() != null && slettebestilling.begrunnelse().length() > SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH) {
			throw new UgyldigSlettebestillingException("Begrunnelse er for lang! Feltet kan maksimalt være %d tegn lang, var %d tegn lang"
					.formatted(SLETTEBESTILLING_BEGRUNNELSE_MAX_LENGTH, slettebestilling.begrunnelse().length()));
		}
	}
}
