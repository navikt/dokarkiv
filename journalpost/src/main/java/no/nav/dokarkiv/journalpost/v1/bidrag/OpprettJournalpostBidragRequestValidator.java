package no.nav.dokarkiv.journalpost.v1.bidrag;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OpprettJournalpostBidragRequestValidator {

	public void validateRequest(OpprettJournalpostRequest request) {
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
			throw new InputValideringFeiletException("Dokumenter skal kun ha én dokumentvariant.");
		} else {
			DokumentVariant dokumentVariant = dokument.getDokumentvarianter().get(0);
			if (!dokumentVariant.getVariantformat().equals("ARKIV")) {
				throw new InputValideringFeiletException("Dokumentet må ha variantformat ARKIV.");
			}
			if (!FilTypeCode.PDF.name().equals(dokumentVariant.getFiltype())) {
				throw new InputValideringFeiletException("Dokumentet må ha filtype PDF.");
			}
			if (dokumentVariant.getFysiskDokument() == null || dokumentVariant.getFysiskDokument().length == 0) {
				throw new InputValideringFeiletException("Det faktiske dokumentet er ikke lagt ved.");
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