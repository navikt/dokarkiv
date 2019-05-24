package no.nav.dokarkiv.journalpost.v1.services;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_JOURNALPOST;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.JournalpostCopier;
import no.nav.dokarkiv.journalpost.v1.validators.KopierJournalpostValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;

@Component
public class KopierJournalpostService {

	private final JoarkRepository joarkRepository;
	private final AksjonsLoggService aksjonsLoggService;
	private final KopierJournalpostValidator kopierJournalpostValidator;
	private final JournalpostCopier journalpostCopier;

	@Inject
	public KopierJournalpostService(final JoarkRepository joarkRepository, final AksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.kopierJournalpostValidator = new KopierJournalpostValidator();
		this.journalpostCopier = new JournalpostCopier();
	}

	public Long execute(Long journalpostId) throws UgyldigAksjonsLoggException {
		// finn journalpost
		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		// verifiser at journalpost er i tilstand som kan kopieres - dvs status = FL, FS eller J, eller har saksrelasjon feilregistrert
		kopierJournalpostValidator.validate(journalpost);

		// kopier journalpost
		Journalpost nyJournalpost = journalpostCopier.copy(journalpost);

		// låse opp den nye journalpost ved å sette den "tilbake" i status: (eks: FS -> D)
		resetJournalpoststatus(nyJournalpost);

        nyJournalpost = joarkRepository.save(nyJournalpost);

        populerAksjonslogg(nyJournalpost.getJournalpostId(), journalpost.getJournalpostId());

		// returnere journalpostId til ny journalpost
		return nyJournalpost.getJournalpostId();
	}

	private void resetJournalpoststatus(Journalpost journalpost) {
		JournalpostTypeCode type = journalpost.getJournalposttype();
		if (JournalpostTypeCode.I.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.OD);
		} else if (JournalpostTypeCode.U.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.R);
		} else { // Notat
			journalpost.setJournalstatus(JournalStatusCode.R);
		}
	}


	private void populerAksjonslogg(long journalpostId, long originalJournalpostId) throws UgyldigAksjonsLoggException {
		AksjonsLoggTO aksjonsLoggTo;
		aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(KOPIER_JOURNALPOST)
				.journalpostId(journalpostId)
				.utfoertAv(MDC.get(MDC_CONSUMER_ID))
				.melding("Journalposten ble kopiert. Id til ny journalpost er " + journalpostId)
				.build();

		ArkivElementEndringTO arkivElementEndringTO = ArkivElementEndringTO.builder()
				.arkivElement("Journalpost.journalpostId")
				.fraVerdi(Long.toString(originalJournalpostId))
				.tilVerdi(Long.toString(journalpostId))
				.build();

		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, Arrays.asList(arkivElementEndringTO));
	}
}