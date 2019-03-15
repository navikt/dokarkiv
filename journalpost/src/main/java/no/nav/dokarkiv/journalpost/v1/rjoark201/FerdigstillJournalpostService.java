package no.nav.dokarkiv.journalpost.v1.rjoark201;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.FERDIGSTILL;
import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class FerdigstillJournalpostService {

	private final JoarkRepository joarkRepository;
	private final JournalpostValidator journalpostValidator;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	@Inject
	public FerdigstillJournalpostService(final JoarkRepository joarkRepository,
										 final AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.journalpostValidator = new JournalpostValidator();
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	public void ferdigstill(Long journalpostId, String journalfoerendeEnhet, String aksjonsloggHeaderString) throws UgyldigAksjonsLoggException {
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		JournalStatusCode prevJournalstatus = journalpost.getJournalstatus();
		String prevJournalfoerendeEnhet = journalpost.getJournalForendeEnhetId();

		validerJournalpost(journalpost);

		ferdigstillJournalpost(journalpost, journalfoerendeEnhet);

		joarkRepository.save(journalpost);

		populerAksjonslogg(journalpostId, aksjonsloggHeaderString, getArkivElementEndringer(journalpost, prevJournalstatus, prevJournalfoerendeEnhet));
	}

	private void validerJournalpost(Journalpost journalpost) {
		journalpostValidator.validateJournalpostTilstand(journalpost);
		journalpostValidator.validateJournalpostStruktur(journalpost);
		journalpostValidator.validatePaakrevdeFelter(journalpost);
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

	private void populerAksjonslogg(Long journalpostId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTo;
		if (isBlank(aksjonsLoggHeaderString)) {
			String bruker = joarkRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new).getBrukere().iterator().next().getBrukerId();
			aksjonsLoggTo = AksjonsLoggTO.builder()
					.aksjon(FERDIGSTILL)
					.journalpostId(journalpostId)
					.utfoertAv(MDC.get(MDC_CONSUMER_ID))
					.bruker(bruker)
					.melding("Journalpost ferdigstilt")
					.build();
		} else {
			aksjonsLoggTo = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, FERDIGSTILL, journalpostId, null);
		}

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
	}

	private List<ArkivElementEndringTO> getArkivElementEndringer(Journalpost journalpost, JournalStatusCode prevJournalstatus, String prevJournalfoerendeEnhet) {
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalpostStatus")
						.fraVerdi(prevJournalstatus == null ? null : prevJournalstatus.name())
						.tilVerdi(journalpost.getJournalstatus().name())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalfEnhet")
						.fraVerdi(prevJournalfoerendeEnhet)
						.tilVerdi(journalpost.getJournalForendeEnhetId())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalfoertAvNavn")
						.fraVerdi(null)
						.tilVerdi(journalpost.getJournalfortAvNavn())
						.build());
	}
}
