package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SlettIkkeVedleggTilknyttJournalpostException;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

public class FjernVedleggTilknyttJournalpostValidator {


	public void validateJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
		if (!VEDLEGG.equals(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom())) {
			throw new SlettIkkeVedleggTilknyttJournalpostException("TilknytteJournalpost er ikke som vedlegg og kan ikke slettes ");
		}
	}

	public void validateJournalPostStatusOgType(Journalpost utgaaendeJournalpost) {
		if (!U.equals(utgaaendeJournalpost.getJournalposttype()) &&
				!D.equals(utgaaendeJournalpost.getJournalstatus())) {
			throw new SlettIkkeVedleggTilknyttJournalpostException(String.format("Kan ikke slette vedlegg med journalpostId=%s, Journalpost må være utgående(U) og under arbeid(D)",
					utgaaendeJournalpost.getJournalpostId()));
		}
	}

	public void validateDokumentInfoOriginalJpNotEqualsInputJournalPost(DokumentInfo dokumentinfo, Long journalpostId) {

		if (dokumentinfo == null && journalpostId==null) {
			throw new DokumentInfoIkkeFunnetException(String.format("Fant ikke source doukument og kan ikke slette vedlgegg med journalpostId=%s",
					journalpostId));
		} else if (dokumentinfo.getOriginalJournalpost().getJournalpostId().equals(journalpostId)) {
			throw new SlettIkkeVedleggTilknyttJournalpostException(String.format("JounalpostId er lik med originalJournalpostId og vedlagt kan ikke slettes. med journalpostId=%s",
					journalpostId));
		}
	}


	public void validateInput(String journalpostId, String dokumentId){
		validateId(journalpostId,"journalpostId");
		validateId(dokumentId,"dokumentinfoId");
	}


}
