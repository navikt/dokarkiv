package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.KanIkkeSlettetVedleggKnyttetTilJournalpostException;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;

public class FjernVedleggTilknyttetJournalpostValidator {


	public void validateJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
		if (!VEDLEGG.equals(journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom())) {
			throw new KanIkkeSlettetVedleggKnyttetTilJournalpostException(format(
					"DokumentInfo med dokmentInfoId=%s er ikke tilknyttet journalpost med journalpostId=%s som vedlegg og kan dermed ikke fjernes.",
					journalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId(),
					journalpostDokumentInfoRelasjon.getJournalpost().getJournalpostId()));
		}
	}

	public void validateJournalPostStatusOgType(Journalpost utgaaendeJournalpost) {
		if (!U.equals(utgaaendeJournalpost.getJournalposttype()) &&
				!D.equals(utgaaendeJournalpost.getJournalstatus())) {
			throw new KanIkkeSlettetVedleggKnyttetTilJournalpostException(format(
					"Kan ikke slette vedlegg fra journalpost. Journalposten må være utgående (journalposttype=U) og under arbeid (journalstatus=D). Den har journalposttype=%s og journalstatus=%s.",
					utgaaendeJournalpost.getJournalpostId(),
					utgaaendeJournalpost.getJournalposttype(),
					utgaaendeJournalpost.getJournalstatus()));
		}
	}

	public void validateDokumentInfoOriginalJpNotEqualsInputJournalpost(DokumentInfo dokumentinfo, Long journalpostId) {
		if (dokumentinfo.getOriginalJournalpost() != null && dokumentinfo.getOriginalJournalpost().getJournalpostId().equals(journalpostId)) {
			throw new KanIkkeSlettetVedleggKnyttetTilJournalpostException(format(
					"Kan ikke fjerne vedlegg fra journalpost hvor vedleggets originalJournalpostId er lik mottatt journalpostId=%s", journalpostId));
		}
	}

	public void validateInput(String journalpostId, String dokumentId){
		validateId(journalpostId,"journalpostId");
		validateId(dokumentId,"dokumentinfoId");
	}


}
