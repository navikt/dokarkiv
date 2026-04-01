package no.nav.dokarkiv.journalpost.v1.api.skjermdokument;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

public enum SkjermDokumentHjemmelCode {
	ARK(SkjermingTypeCode.FEIL),
	POL(SkjermingTypeCode.POL);

	private SkjermingTypeCode skjermingTypeCode;

	SkjermDokumentHjemmelCode(SkjermingTypeCode skjermingTypeCode) {
		this.skjermingTypeCode = skjermingTypeCode;
	}
	public SkjermingTypeCode asSkjermingTypeCode() {
		return this.skjermingTypeCode;
	}

}
