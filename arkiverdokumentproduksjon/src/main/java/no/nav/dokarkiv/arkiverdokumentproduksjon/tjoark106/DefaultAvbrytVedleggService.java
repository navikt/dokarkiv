package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.repository.dok.joark.mod.JoarkRepository;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.NoDokumentInfoFoundException;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.journalbehandling.UgyldigDokumentStatusVerdiException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusVerdiException;
import no.nav.service.dok.joark.nsb.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.service.dok.joark.nsb.to.AvbrytVedleggRequestTo;

import javax.inject.Inject;

/**
 * Implementation of TJOARK106 - AvbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class DefaultAvbrytVedleggService implements AvbrytVedleggService {

	@Inject
	private AvbrytVedleggValidator validator;

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	public void avbrytVedlegg(AvbrytVedleggRequestTo request) throws NoJournalpostFoundException,
			NoDokumentInfoFoundException, UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException,
			UgyldigTilknyttetJournalpostSomVerdiException {
		validator.validateInputRequest(request);

		Journalpost journalpost = joarkRepository.findJournalpostById(request.getJournalpostId());
		validator.validateJournalpost(journalpost, request.getJournalpostId());

		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		validator.validateDokumentInfo(dokumentInfo, request.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon =
				dokumentInfo.findJournalpostRelasjonByJournalpostId(request.getJournalpostId());
		validator.validateJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);

		if (dokumentInfo.isRelatedToMultipleJournalposts()) {
			joarkRepository.deleteJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);
		} else {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.AVBRUTT);
		}

		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
	}

}
