package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

@Component
public class DefaultAvbrytVedleggService implements AvbrytVedleggService {

	private final AvbrytVedleggValidator validator;
	private final JoarkRepositorySkjermet joarkRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final SporingPopulator sporingPopulator;

	public DefaultAvbrytVedleggService(AvbrytVedleggValidator validator, JoarkRepositorySkjermet joarkRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, SporingPopulator sporingPopulator) {
		this.validator = validator;
		this.joarkRepository = joarkRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.sporingPopulator = sporingPopulator;
	}

	@Override
	public void avbrytVedlegg(AvbrytVedleggRequestTo request) throws NoJournalpostFoundException,
			NoDokumentInfoFoundException, UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException,
			UgyldigTilknyttetJournalpostSomVerdiException {
		validator.validateInputRequest(request);

		final Long journalpostId = request.getJournalpostId();
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new NoJournalpostFoundException("journalpostid=" + journalpostId + " does not exist", journalpostId));
		validator.validateJournalpost(journalpost, journalpostId);

		DokumentInfo dokumentInfo = journalpost.findDokumentInfoById(request.getDokumentInfoId());
		validator.validateDokumentInfo(dokumentInfo, request.getDokumentInfoId());

		JournalpostDokumentInfoRelasjon dokumentInfoRelasjon =
				dokumentInfo.findJournalpostRelasjonByJournalpostId(journalpostId);
		validator.validateJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);

		if (dokumentInfo.isRelatedToMultipleJournalposts()) {
			dokumentInfoRelasjon.getDokumentInfo().removeJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);
			dokumentInfoRelasjon.getJournalpost().removeJournalpostDokumentInfoRelasjon(dokumentInfoRelasjon);
			journalpostDokumentInfoRelasjonRepository.delete(dokumentInfoRelasjon);
		} else {
			dokumentInfo.setDokumentstatus(DokumentStatusCode.AVBRUTT);
		}
		sporingPopulator.populateSporingInfo(journalpost, request.getEndretAvNavn());
	}

}
