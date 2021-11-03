package no.nav.dokarkiv.journalpost.v1.bidrag;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

class OpprettJournalpostBidragRequestValidator {

	private static final String BIDRAG_GYLDIG_TEMA = "BID";

	public void validateRequest(OpprettJournalpostRequest request) {
		if (request.getTema() != null && !BIDRAG_GYLDIG_TEMA.equals(request.getTema())) {
			throw new InputValideringFeiletException("Kan ikke opprette bidrag mellomlagring. Tema må være BID.");
		}

		if (request.getAvsenderMottaker() != null) {
			validateAvsenderMottaker(request.getAvsenderMottaker());
		}

		if (request.getDatoMottatt() == null) {
			throw new InputValideringFeiletException("Kan ikke opprette bidrag uten DatoMottatt.");
		}

		if (!request.getDokumenter().isEmpty()) {
			request.getDokumenter().forEach(this::validateDokument);
		} else {
			throw new InputValideringFeiletException("Kan ikke opprette bidrag uten dokumenter.");
		}
	}

	private void validateDokument(Dokument dokument) {
		if (dokument.getDokumentvarianter().size() != 1) {
			throw new InputValideringFeiletException("Listen dokument.dokumentVarianter skal kun ha ett element (én dokumentvariant).");
		} else {
			DokumentVariant dokumentVariant = dokument.getDokumentvarianter().get(0);
			if (!dokumentVariant.getVariantformat().equals(VariantFormatCode.ARKIV.name())) {
				throw new InputValideringFeiletException("dokumentVariant.variantFormat må ha variantformat ARKIV.");
			}
			if (!FilTypeCode.PDF.name().equals(dokumentVariant.getFiltype())) {
				throw new InputValideringFeiletException("dokumentVariant.filtype må ha filtype PDF.");
			}
			if (dokumentVariant.getFysiskDokument() == null || dokumentVariant.getFysiskDokument().length == 0) {
				throw new InputValideringFeiletException("dokumentVariant.fysiskDokument mangler data.");
			}
		}
	}

	private void validateAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		if (isNotBlank(avsenderMottaker.getId()) && avsenderMottaker.getIdType() == null) {
			throw new InputValideringFeiletException("AvsenderMottaker.idType må være satt når AvsenderMottaker.id er satt.");
		}
		if (avsenderMottaker.getIdType() != null && isBlank(avsenderMottaker.getId())) {
			throw new InputValideringFeiletException("AvsenderMottaker.id må være satt når AvsenderMottaker.idType er satt.");
		}
		if (avsenderMottaker.getIdType() == AvsenderMottakerIdType.FNR) {
			if (!avsenderMottaker.getId().matches("^\\d{11}$")) {
				throw new InputValideringFeiletException("AvsenderMottaker.id må være 11 siffer for et FNR.");
			}
		} else {
			throw new InputValideringFeiletException("AvsenderMottaker.idType må ha typen FNR.");
		}
	}
}