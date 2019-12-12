package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.JournalpostCopier;
import no.nav.dokarkiv.journalpost.v1.validators.KopierJournalpostValidator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;

@Component
public class KopierJournalpostService {
	private static final String SRV_JOARKADMIN = "srvjoarkadmin";
	private final JoarkRepository joarkRepository;
	private final LagreAksjonsLoggService aksjonsLoggService;
	private final KopierJournalpostValidator kopierJournalpostValidator;
	private final JournalpostCopier journalpostCopier;

	@Inject
	public KopierJournalpostService(final JoarkRepository joarkRepository, final LagreAksjonsLoggService aksjonsLoggService) {
		this.joarkRepository = joarkRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.kopierJournalpostValidator = new KopierJournalpostValidator();
		this.journalpostCopier = new JournalpostCopier();
	}

	public Long execute(Long journalpostId) {
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

        Long nyJournalpostId = nyJournalpost.getJournalpostId();

		ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
				.arkivElement(JOURNALPOST_JOURNALPOST_ID)
				.fraVerdi(Long.toString(journalpost.getJournalpostId()))
				.tilVerdi(Long.toString(nyJournalpostId))
				.build();

		aksjonsLoggService.lagreAksjonsLoggForJournalpost(
				AksjonsTypeCode.KOPIER_JOURNALPOST, journalpostId, null,"Journalposten ble kopiert. Id til ny journalpost er " + nyJournalpostId,
				SRV_JOARKADMIN, Collections.singletonList(endring));

		// returnere journalpostId til ny journalpost
		return nyJournalpostId;
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
}