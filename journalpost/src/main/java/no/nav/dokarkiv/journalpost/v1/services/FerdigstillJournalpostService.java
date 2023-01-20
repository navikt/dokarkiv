package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.FerdigstillJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.validators.FerdigstillJournalpostValidator;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.time.LocalDateTime.now;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.FERDIGSTILL;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateJournalfoerendeEnhet;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
@Slf4j
public class FerdigstillJournalpostService {

	private final JournalpostRepository journalpostRepository;
	private final FerdigstillJournalpostValidator ferdigstillJournalpostValidator;
	private final AksjonsLoggService aksjonsLoggService;

	public FerdigstillJournalpostService(final JournalpostRepository journalpostRepository,
										 final AksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.ferdigstillJournalpostValidator = new FerdigstillJournalpostValidator();
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void setJournalfoerendeEnhetNull(Long journalpostId, String journalfoerendeEnhet) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		oppdatertJournalpost(journalpost, journalfoerendeEnhet);
		log.info("Oppdatert journalpostId={} med journalfoerendeEnhet={}", journalpostId, journalpost.getJournalForendeEnhetId());
	}

	public void ferdigstill(Long journalpostId, FerdigstillJournalpostRequest ferdigstillJournalpostRequest) {
		// Kaller fetchById for å hente alle relevante data i en spørring. Siden validerJournalpost sjekker store deler av entitetsgrafen til Journalpost
		Journalpost journalpost = journalpostRepository.fetchById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		JournalStatusCode prevJournalstatus = journalpost.getJournalstatus();
		String prevJournalfoerendeEnhet = journalpost.getJournalForendeEnhetId();
		String prevJournalfortAvNavn = journalpost.getJournalfortAvNavn();

		validerJournalpost(journalpost);
		setJournalpostStatus(journalpost);
		this.oppdatertJournalpost(journalpost, ferdigstillJournalpostRequest);

		populerAksjonslogg(journalpostId, getArkivElementEndringer(journalpost, prevJournalstatus, prevJournalfoerendeEnhet, prevJournalfortAvNavn));
	}

	public void ferdigstill(Long journalpostId, String journalfoerendeEnhet) {
		// Kaller findById i stedet for fetch siden Journalpost har vært managed i samme tråd. Da hentes den fra JPA first level cache.
		Journalpost journalpost = journalpostRepository.findById(journalpostId).
				orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		JournalStatusCode prevJournalstatus = journalpost.getJournalstatus();
		String prevJournalfoerendeEnhet = journalpost.getJournalForendeEnhetId();
		String prevJournalfortAvNavn = journalpost.getJournalfortAvNavn();

		validerJournalpost(journalpost);
		setJournalpostStatus(journalpost);
		oppdatertJournalpost(journalpost, journalfoerendeEnhet);

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
		} catch (JournalpostIkkeMidlertidigException e) {
			log.info(MDC.get(MDC_REQUEST_ID) + " kunne ikke ferdigstille journalpost. Er endelig journalført fra før. journalpostId={}.", journalpostId);
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

	@Deprecated // skal bli fjernet når migrering fra ondemand til Joark er ferdig, gjelder sak MMA-5695.
	private void oppdatertJournalpost(Journalpost journalpost, FerdigstillJournalpostRequest journalfoerendeEnhet) {
		journalpost.setJournalDato(
				journalfoerendeEnhet.getDatoJournal() != null ? journalfoerendeEnhet.getDatoJournal() :
						Date.from(now().atZone(ZoneId.systemDefault()).toInstant())
		);
		journalpost.setJournalForendeEnhetId(journalfoerendeEnhet.getJournalfoerendeEnhet());
		journalpost.setEndretAvNavn(MDC.get(MDC_USER_NAME));
		journalpost.setJournalfortAvNavn(
				journalfoerendeEnhet.getJournalfortAvNavn() != null ? journalfoerendeEnhet.getJournalfortAvNavn() : MDC.get(MDC_USER_NAME)
		);
		journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
		if (!isBlank(journalfoerendeEnhet.getOpprettetAvNavn())) {
			journalpost.setOpprettetAvNavn(journalfoerendeEnhet.getOpprettetAvNavn());
		}
		if (journalfoerendeEnhet.getDatoSendtPrint() != null) {
			journalpost.setSendtPrintDato(journalfoerendeEnhet.getDatoSendtPrint());
		}
	}

	private void oppdatertJournalpost(Journalpost journalpost, String journalfoerendeEnhet) {
		journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		journalpost.setJournalForendeEnhetId(journalfoerendeEnhet);
		journalpost.setEndretAvNavn(MDC.get(MDC_USER_NAME));
		journalpost.setJournalfortAvNavn(MDC.get(MDC_USER_NAME));
		journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	private void setJournalpostStatus(Journalpost journalpost) {
		if (I.equals(journalpost.getJournalposttype())) {
			journalpost.setJournalstatus(JournalStatusCode.J);
		} else if (JournalpostTypeCode.U.equals(journalpost.getJournalposttype())) {
			if (L.equals(journalpost.getUtsendingskanal())) {
				journalpost.setJournalstatus(JournalStatusCode.FL);
			} else {
				journalpost.setJournalstatus(JournalStatusCode.FS);
			}
		} else { // JournalpostTypeCode.N
			journalpost.setJournalstatus(JournalStatusCode.FL);
		}
	}

	private void populerAksjonslogg(Long journalpostId, List<ArkivElementEndringTO> arkivElementEndringTOList) {
		String bruker = journalpostRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new).getBrukere().iterator().next().getBrukerId();
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
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
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

		if (isBlank(prevJournalfoerendeEnhet) || !prevJournalfoerendeEnhet.equals(journalpost.getJournalForendeEnhetId())) {
			arkivElementEndringTOList.add(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.journalfEnhet")
					.fraVerdi(prevJournalfoerendeEnhet)
					.tilVerdi(journalpost.getJournalForendeEnhetId())
					.build());
		}

		if (isBlank(prevJournalfortAvNavn) || !prevJournalfortAvNavn.equals(journalpost.getJournalfortAvNavn())) {
			arkivElementEndringTOList.add(ArkivElementEndringTO.builder()
					.arkivElement("Journalpost.journalfoertAvNavn")
					.fraVerdi(prevJournalfortAvNavn)
					.tilVerdi(journalpost.getJournalfortAvNavn())
					.build());
		}
		return arkivElementEndringTOList;
	}
}