package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.FERDIGSTILL;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateJournalfoerendeEnhet;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FerdigstillJournalpostService {

	private final JoarkRepository joarkRepository;
	private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
	private final AksjonsLoggService aksjonsLoggService;

	@Inject
	public FerdigstillJournalpostService(final JoarkRepository joarkRepository,
										 final AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void ferdigstill(Long journalpostId, String journalfoerendeEnhet) {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		JournalStatusCode prevJournalstatus = journalpost.getJournalstatus();
		String prevJournalfoerendeEnhet = journalpost.getJournalForendeEnhetId();
		String prevJournalfortAvNavn = journalpost.getJournalfortAvNavn();

		validerJournalpost(journalpost);

		ferdigstillJournalpost(journalpost, journalfoerendeEnhet);

		joarkRepository.save(journalpost);


		populerAksjonslogg(journalpostId, getArkivElementEndringer(journalpost, prevJournalstatus, prevJournalfoerendeEnhet, prevJournalfortAvNavn));
	}

	public Pair<String, String> forsoekFerdigstill(Long journalpostId, OpprettJournalpostRequest request) {
		log.info(MDC.get(MDC_REQUEST_ID) + " forsøker å ferdigstille journalpost, journalpostId={}", journalpostId);
		Pair<String, String> ferdigstillResponse;
		try {
			validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet");
			ferdigstill(journalpostId, request.getJournalfoerendeEnhet());
			log.info(MDC.get(MDC_REQUEST_ID) + " har ferdigstilt journalpost, journalpostId={}", journalpostId);
			ferdigstillResponse = Pair.of("ENDELIG", null);
		} catch (DokarkivFunctionalException e) {
			log.info(MDC.get(MDC_REQUEST_ID) + " kunne ikke ferdigstille journalpost, journalpostId={}. {}", journalpostId, e.getMessage());
			ferdigstillResponse = Pair.of("MIDLERTIDIG", e.getMessage());
		}
		return ferdigstillResponse;
	}

	private void validerJournalpost(Journalpost journalpost) {
		ferdigstillJournalpostValidator.validateJournalpostTilstand(journalpost);
		ferdigstillJournalpostValidator.validateJournalpostStruktur(journalpost);
		ferdigstillJournalpostValidator.validatePaakrevdeFelter(journalpost);
	}

	private void ferdigstillJournalpost(Journalpost journalpost, String journalfoerendeEnhet) {
		setJournalpostStatus(journalpost);
		journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		journalpost.setJournalForendeEnhetId(journalfoerendeEnhet);
		journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
		journalpost.setJournalfortAvNavn(MDC.get(MDC_USER_ID));
		journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	private void setJournalpostStatus(Journalpost journalpost) {
		if (JournalpostTypeCode.I.equals(journalpost.getJournalposttype())) {
			journalpost.setJournalstatus(JournalStatusCode.J);
		} else if (JournalpostTypeCode.U.equals(journalpost.getJournalposttype())) {
			journalpost.setJournalstatus(JournalStatusCode.FS);
		} else { // JournalpostTypeCode.N
			journalpost.setJournalstatus(JournalStatusCode.FS);
		}
	}

	private void populerAksjonslogg(Long journalpostId, List<ArkivElementEndringTO> arkivElementEndringTOList) {
		String bruker = joarkRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new).getBrukere().iterator().next().getBrukerId();
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(FERDIGSTILL)
				.journalpostId(journalpostId)
				.utfoertAv(MDC.get(MDC_CONSUMER_ID))
				.bruker(bruker)
				.melding("Journalpost ferdigstilt")
				.build();

		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: "+e.getMessage());
		}
	}

	private List<ArkivElementEndringTO> getArkivElementEndringer(Journalpost journalpost, JournalStatusCode prevJournalstatus, String prevJournalfoerendeEnhet, String prevJournalfortAvNavn) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		// Journalpost skifter _alltid_ status ved ferdigstilling
		arkivElementEndringTOList.add(ArkivElementEndringTO.builder()
				.arkivElement("Journalpost.journalpostStatus")
				.fraVerdi(prevJournalstatus == null ? null : prevJournalstatus.name())
				.tilVerdi(journalpost.getJournalstatus().name())
				.build());

		if (isBlank(prevJournalfoerendeEnhet) || !prevJournalfoerendeEnhet.equals(journalpost.getJournalForendeEnhetId())){
			arkivElementEndringTOList.add(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.journalfEnhet")
					.fraVerdi(prevJournalfoerendeEnhet)
					.tilVerdi(journalpost.getJournalForendeEnhetId())
					.build());
		}

		if (isBlank(prevJournalfortAvNavn) || !prevJournalfortAvNavn.equals(journalpost.getJournalfortAvNavn())){
			arkivElementEndringTOList.add(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.journalfoertAvNavn")
					.fraVerdi(prevJournalfortAvNavn)
					.tilVerdi(journalpost.getJournalfortAvNavn())
					.build());
		}
		return arkivElementEndringTOList;
	}
}