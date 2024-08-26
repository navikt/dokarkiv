package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.FeilregistreringAlleredeOpphevetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeKnyttetTilSakException;
import no.nav.dokarkiv.core.exceptions.SaksrelasjonAlleredeFeilregistrertException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.core.repository.SaksrelasjonRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.journalpost.v1.controllers.FeilregistrerJournalpostRestController.FEILREGISTRERING_OPPHEVET_MESSAGE;

@Slf4j
@Component
public class FeilregistrerSakstilknytningService {

	private final AksjonsLoggService aksjonsLoggService;
	private final JournalpostRepository journalpostRepository;
	private final SaksrelasjonRepository saksrelasjonRepository;

	public FeilregistrerSakstilknytningService(final AksjonsLoggService aksjonsLoggService,
											   final JournalpostRepository journalpostRepository,
											   final SaksrelasjonRepository saksrelasjonRepository) {
		this.aksjonsLoggService = aksjonsLoggService;
		this.journalpostRepository = journalpostRepository;
		this.saksrelasjonRepository = saksrelasjonRepository;
	}

	@Transactional
	public void feilregistrerSakstilknytning(long journalpostId) {
		Saksrelasjon saksrelasjon = hentSaksRelasjonForJournalpost(journalpostId);

		if (saksrelasjon.getFeilregistrert() == null || !saksrelasjon.getFeilregistrert()) {
			saksrelasjon.setFeilregistrert(true);
			saksrelasjon.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
			saksrelasjon.setEndretAvNavn(MDC.get(MDC_USER_NAME));
		} else {
			throw new SaksrelasjonAlleredeFeilregistrertException("Saksrelasjonen er allerede feilregistrert");
		}

		List<ArkivElementEndringTO> arkivElementEndringTOS = Collections.singletonList(ArkivElementEndringTO.builder()
				.arkivElement("Journalpost.Saksrelasjon.feilregistrert")
				.fraVerdi("false")
				.tilVerdi("true")
				.build());
		populerAksjonslogg(journalpostId, AksjonsTypeCode.FEILREGISTRER_SAKSTILKNYTNING, arkivElementEndringTOS, "Saksrelasjonen ble feilregistrert");
	}

	@Transactional
	public void opphevFeilregistrertSakstilknytning(long journalpostId) {
		Saksrelasjon saksrelasjon = hentSaksRelasjonForJournalpost(journalpostId);

		if (saksrelasjon.getFeilregistrert() == null || !saksrelasjon.getFeilregistrert()) {
			throw new FeilregistreringAlleredeOpphevetException("Feilregistreringen er allerede opphevet");
		} else {
			saksrelasjon.setFeilregistrert(false);
		}

		List<ArkivElementEndringTO> arkivElementEndringTOS = Collections.singletonList(ArkivElementEndringTO.builder()
				.arkivElement("Journalpost.Saksrelasjon.feilregistrert")
				.fraVerdi("true")
				.tilVerdi("false")
				.build());
		populerAksjonslogg(journalpostId, AksjonsTypeCode.OPPHEV_FEILREGISTRERING, arkivElementEndringTOS, FEILREGISTRERING_OPPHEVET_MESSAGE);
	}

	private Saksrelasjon hentSaksRelasjonForJournalpost(Long journalpostId) {
		assertJournalpostExists(journalpostId);
		return saksrelasjonRepository.findByJournalpostId(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeKnyttetTilSakException("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak"));
	}

	private void assertJournalpostExists(Long journalpostId) {
		if (!journalpostRepository.existsById(journalpostId)) {
			throw new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId));
		}
	}

	private void populerAksjonslogg(long journalpostId, AksjonsTypeCode aksjon, List<ArkivElementEndringTO> arkivElementEndringTOList, String melding) {
		AksjonsLoggTO aksjonsLoggTo;
		aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.hjemmel("ARKL")
				.melding(melding)
				.build();
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
		} catch (UgyldigAksjonsLoggException e) {
			log.error("Kunne ikke skrive til AksjonsLogg: " + e.getMessage(), e);
		}
	}
}