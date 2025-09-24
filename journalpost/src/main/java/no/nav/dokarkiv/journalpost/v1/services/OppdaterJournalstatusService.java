package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.ChangeTracker;
import no.nav.dokarkiv.journalpost.v1.util.oppdaterjournalpost.JournalpostUpdater;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.TILBAKE_TIL_MOTTATT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UKJENT_BRUKER;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalstatusValidator.validateOppdaterJournalstatus;

@Service
@Slf4j
public class OppdaterJournalstatusService {
	private final JournalpostRepository journalpostRepository;
	private final JournalpostUpdater journalpostUpdater;
	private final AksjonsLoggService aksjonsLoggService;

	public OppdaterJournalstatusService(JournalpostRepository journalpostRepository,
										JournalpostUpdater journalpostUpdater,
										AksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.journalpostUpdater = journalpostUpdater;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterJournalstatus(long journalpostId, JournalStatusCode newStatus) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		validateOppdaterJournalstatus(journalpost);

		ChangeTracker changeTracker = journalpostUpdater.changeJournalstatus(journalpost, newStatus);
		if (!changeTracker.getChanges().isEmpty()) {
			populerAksjonsloggOppdaterJournalstatus(journalpostId, newStatus, changeTracker);
		}
	}

	private void populerAksjonsloggOppdaterJournalstatus(long journalpostId, JournalStatusCode newStatus, ChangeTracker changeTracker) {
		try {
			AksjonsTypeCode aksjonsTypeCode = switch (newStatus) {
				case UB -> UKJENT_BRUKER;
				case U -> UTGAAR;
				case M -> TILBAKE_TIL_MOTTATT;
				default ->
						throw new UgyldigAksjonsLoggException("Kunne ikke finne riktig aksjonstype for oppdaterJournalstatus til " + newStatus);
			};
			AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
					.aksjon(aksjonsTypeCode)
					.journalpostId(journalpostId)
					.hjemmel(null)
					.melding("oppdaterJournalstatus oppdaterte journalstatus")
					.build();
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, changeTracker.getChanges());
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}
}
