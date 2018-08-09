package no.nav.dokarkiv.journalfoerInngaaende.v1.service;

import static org.springframework.util.StringUtils.hasText;

import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class HentJournalpostByJournalpostIdService {

	private JoarkRepository joarkRepository;
	private DokumentinfoRepository dokumentinfoRepository;

	@Inject
	public HentJournalpostByJournalpostIdService(JoarkRepository joarkRepository,
												 DokumentinfoRepository dokumentinfoRepository) {
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

//	JournalpostResponseTo hentJournalpost(String journalpostIdString) throws DokmotArkivRestFunctionalException {
//
//		hasText(journalpostIdString);
//		Long journalpostId = convertStringToLong(journalpostIdString, "journalpostId");
//
//		Journalpost journalpost = joarkRepository.findById(journalpostId)
//				.orElseThrow(() -> new DokmotArkivRestFunctionalException(String.format("Kunne ikke finne journalpost med journalpostId=%s i Joark", journalpostId), HttpStatus.BAD_REQUEST));
//
//
//		DokumentInfo dokumentInfo = dokumentinfoRepository.findById(journalpost.getJournalpostDokumentInfoRelasjoner())
//				.orElseThrow(() -> new DokmotArkivRestFunctionalException(String.format("Kunne ikke finne journalpost med journalpostId=%s i Joark", journalpostId), HttpStatus.BAD_REQUEST));
//
//	}
//
//	JournalpostResponseTo.JournalpostResponseToBuilder  mapJournalpost(Journalpost journalpost, JournalpostResponseTo.JournalpostResponseToBuilder journalpostResponseToBuilder){
//		journalpostResponseToBuilder
//				.journaltilstand()
//				.avsender()
//				.brukere()
//				.arkivsak()
//				.tema()
//				.tittel()
//				.kanalreferanseId(journalpost.getKanalReferanseId())
//				.forsendelseMottatt(journalpost.getMottattDato())
//				.mottakskanal(journalpost.getMottakskanal())
//				.journalfoerendeEnhet(journalpost.getJournalForendeEnhetId())
//	}
}
