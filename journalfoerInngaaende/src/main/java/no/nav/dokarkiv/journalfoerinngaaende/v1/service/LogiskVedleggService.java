package no.nav.dokarkiv.journalfoerinngaaende.v1.service;

import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class LogiskVedleggService {

	private JoarkRepository joarkRepository;
	private SkannetInnholdRepository skannetInnholdRepository;

	@Inject
	public LogiskVedleggService(JoarkRepository joarkRepository,
								SkannetInnholdRepository skannetInnholdRepository) {
		this.joarkRepository = joarkRepository;
		this.skannetInnholdRepository = skannetInnholdRepository;
	}

	//TODO: Sporingsinfo
	public void deleteLogiskVedlegg(String journalpostIdString, String dokumentIdString, String logiskVedleggIdString) throws DokarkivRestFunctionalException {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, "dokumentId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new DokarkivRestFunctionalException("Kunne ikke finne journalpost i Joark", HttpStatus.NOT_FOUND));

		validateJournalpostIsInngaaende(journalpost);
		assertDokumentinfo(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId), journalpostIdString, dokumentIdString);

		skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString)
				.orElseThrow(() -> new DokarkivRestFunctionalException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggIdString, dokumentIdString),
						HttpStatus.NOT_FOUND));

		skannetInnholdRepository.deleteSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString);
	}

	//TODO: Sporingsinfo
	public void updateLogiskVedlegg(String journalpostIdString, String dokumentIdString, String logiskVedleggIdString, PutLogiskVedleggRequest request) throws DokarkivRestFunctionalException {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, "dokumentId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new DokarkivRestFunctionalException("Kunne ikke finne journalpost i Joark", HttpStatus.NOT_FOUND));

		validateJournalpostIsInngaaende(journalpost);
		assertDokumentinfo(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId), journalpostIdString, dokumentIdString);

		SkannetInnhold skannetInnhold = skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString)
				.orElseThrow(() -> new DokarkivRestFunctionalException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggIdString, dokumentIdString),
						HttpStatus.NOT_FOUND));

		skannetInnhold.setVedleggInnhold(request.getTittel());

		skannetInnholdRepository.save(skannetInnhold);
	}


	//TODO: Sporingsinfo
	public Long persistLogiskVedlegg(String journalpostIdString, String dokumentIdString, PostLogiskVedleggRequest request) throws DokarkivRestFunctionalException {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, "journalpostId");
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, "dokumentId");

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new DokarkivRestFunctionalException("Kunne ikke finne journalpost i Joark", HttpStatus.NOT_FOUND));

		validateJournalpostIsInngaaende(journalpost);

		DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId);
		assertDokumentinfo(dokumentInfo, journalpostIdString, dokumentIdString);

		SkannetInnhold skannetInnhold = SkannetInnhold.builder().vedleggInnhold(request.getTittel()).build();

		skannetInnhold = skannetInnholdRepository.save(skannetInnhold);

		dokumentInfo.addSkannetInnhold(skannetInnhold);

		return skannetInnhold.getSkannetInnholdId();
	}

	private void validateJournalpostIsInngaaende(Journalpost journalpost) {
		if (!journalpost.isInngaende()) {
			throw new DokarkivRestFunctionalException("Journalpost er ikke av type Inngaaende", HttpStatus.BAD_REQUEST); //TODO Annen HttpStatus?
		}
	}

	private void assertDokumentinfo(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokarkivRestFunctionalException(String.format("Finner ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId), HttpStatus.NOT_FOUND); //TODO Annen HttpStatus?
		}
	}

}