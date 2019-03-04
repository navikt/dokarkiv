package no.nav.dokarkiv.journalpost.v1.rjoark201;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
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

	@Inject
	public FerdigstillJournalpostService(final JoarkRepository joarkRepository,
										 final JournalpostValidator journalpostValidator) {
		this.joarkRepository = joarkRepository;
		this.journalpostValidator = journalpostValidator;
	}

	public List<ArkivElementEndringTO> ferdigstill(String journalpostId, String journalfEnhet) {
		Journalpost journalpost = joarkRepository.findById(parseLong(journalpostId))
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		JournalStatusCode prevStatus = journalpost.getJournalstatus();

		validerJournalpost(journalpost);

		ferdigstillJournalpost(journalpost, journalfEnhet);

		joarkRepository.save(journalpost);

		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalpostStatus")
						.fraVerdi(prevStatus.name())
						.tilVerdi(journalpost.getJournalstatus().name())
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalfEnhet")
						.fraVerdi(null)
						.tilVerdi(journalfEnhet)
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalfoertAvNavn")
						.fraVerdi(null)
						.tilVerdi(journalpost.getJournalfortAvNavn())
						.build());
	}

	private void validerJournalpost(Journalpost journalpost) {
		journalpostValidator.validateJournalpostTilstand(journalpost);
		journalpostValidator.validateJournalpostStruktur(journalpost);
		journalpostValidator.validatePaakrevdeFelter(journalpost);
	}

	private void ferdigstillJournalpost(Journalpost journalpost, String journalfEnhet) {
		setJournalpostStatus(journalpost);
		journalpost.setJournalDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
		journalpost.setJournalForendeEnhetId(journalfEnhet);
		journalpost.setEndretAvNavn(MDC.get(MDC_USER_ID));
		journalpost.setJournalfortAvNavn(MDC.get(MDC_USER_ID));
		journalpost.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	private void setJournalpostStatus(Journalpost journalpost) {
		if (JournalpostTypeCode.I.equals(journalpost.getJournalposttype())){
			journalpost.setJournalstatus(JournalStatusCode.J);
		} else if (JournalpostTypeCode.U.equals(journalpost.getJournalposttype())){
			journalpost.setJournalstatus(JournalStatusCode.FS);
		} else { // JournalpostTypeCode.N
			journalpost.setJournalstatus(JournalStatusCode.FS);
		}
	}
}
