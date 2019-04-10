package no.nav.dokarkiv.journalpost.v1.rjoark203;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.rjoark203.support.JournalpostCopier;
import no.nav.dokarkiv.journalpost.v1.rjoark203.support.KopierJournalpostValidator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class KopierJournalpostService {

	private final JoarkRepository joarkRepository;
	private final KopierJournalpostValidator kopierJournalpostValidator;
	private final JournalpostCopier journalpostCopier;

	@Inject
	public KopierJournalpostService(final JoarkRepository joarkRepository) {
		this.joarkRepository = joarkRepository;
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

		// returnere journalpostId til ny journalpost
		return nyJournalpost.getJournalpostId();
	}

	private void resetJournalpoststatus(Journalpost journalpost) {
		JournalpostTypeCode type = journalpost.getJournalposttype();
		if (JournalpostTypeCode.I.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.M);
		} else if (JournalpostTypeCode.U.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.D);
		} else { // Notat
			journalpost.setJournalstatus(JournalStatusCode.D);
		}
	}
}