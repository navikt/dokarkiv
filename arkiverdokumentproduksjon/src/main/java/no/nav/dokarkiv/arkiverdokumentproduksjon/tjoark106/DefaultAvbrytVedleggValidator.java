package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import static no.nav.domain.dok.joark.codestable.DokumentStatusCode.AVBRUTT;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.JournalStatusCode;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.NoDokumentInfoFoundException;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.service.dok.joark.nsb.to.AvbrytVedleggRequestTo;
import org.springframework.util.Assert;

/**
 * Validator class for TJOARK106 - AvbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class DefaultAvbrytVedleggValidator implements AvbrytVedleggValidator {

	@Override
	public void validateJournalpost(Journalpost journalpost, Long journalpostId) throws UgyldigJournalStatusVerdiException,
			NoJournalpostFoundException {
		if (journalpost == null) {
			throw new NoJournalpostFoundException("journalpostid=" + journalpostId + " does not exist",
					journalpostId);
		}
		if (!JournalStatusCode.D.equals(journalpost.getJournalstatus())) {
			throw new UgyldigJournalStatusVerdiException("Invalid JournalStatus for journalpostid="
					+ journalpostId, journalpost.getJournalstatus());
		}
	}

	@Override
	public void validateDokumentInfo(DokumentInfo dokumentInfo, Long dokumentInfoId) throws NoDokumentInfoFoundException,
			UgyldigDokumentStatusVerdiException {
		if (dokumentInfo == null) {
			throw new NoDokumentInfoFoundException("Journalpost missing DokumentInfo with dokumentinfoid=" + dokumentInfoId,
					dokumentInfoId);
		}

		if (AVBRUTT.equals(dokumentInfo.getDokumentstatus())) {
			throw new UgyldigDokumentStatusVerdiException("dokumentinfoid=" + dokumentInfo.getDokumentInfoId()
					+ " is already Avbrutt", AVBRUTT);
		}
	}

	@Override
	public void validateJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon)
			throws UgyldigTilknyttetJournalpostSomVerdiException {
		if (!journalpostDokumentInfoRelasjon.isVedlegg()) {
			throw new UgyldigTilknyttetJournalpostSomVerdiException("tilknyttetjournalpostsom=" +
					journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom() + " is not Vedlegg on relasjon " +
					"journalpostid=" + journalpostDokumentInfoRelasjon.getJournalpost().getJournalpostId() + " " +
					"dokumentinfoid=" + journalpostDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId(),
					journalpostDokumentInfoRelasjon.getTilknyttetJournalpostSom());
		}
	}

	@Override
	public void validateInputRequest(AvbrytVedleggRequestTo request) {
		if (request.getJournalpostId() == null || request.getJournalpostId() == 0) {
			throw new IllegalArgumentException("JournalpostId cannot be empty or missing. " + request);
		}
		if (request.getDokumentInfoId() == null || request.getDokumentInfoId() == 0) {
			throw new IllegalArgumentException("DokumentInfoId cannot be empty or missing. " + request);
		}
		Assert.hasText(request.getEndretAvNavn(), "EndretAvNavn cannot be empty or missing. " + request);
	}
}
