package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.repository.dok.joark.JoarkRepository;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.UgyldigJournalStatusOvergangException;
import no.nav.service.dok.joark.nsb.to.AvbrytJournalpostRequestTo;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * Implementation of the AvbrytJournalpostService
 *
 * @author Stig Strøm
 */
public class DefaultAvbrytJournalpostService implements AvbrytJournalpostService {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private AvbrytJournalpostUpdater avbrytJournalpostUpdater;

	@Inject
	private AvbrytJournalpostValidator validator;

	@Override
	public void avbrytJournalpost(AvbrytJournalpostRequestTo domainRequest) throws NoJournalpostFoundException,
			UgyldigJournalStatusOvergangException {
		Assert.notNull(domainRequest, "Request cannot be empty or missing");
		domainRequest.validate();

		Journalpost journalpost = joarkRepository.findJournalpostByJournalpostId(domainRequest.getJournalpostId(), false);
		if (journalpost == null) {
			throw new NoJournalpostFoundException("Journalpost with id: " + domainRequest.getJournalpostId() + " not found",
					domainRequest.getJournalpostId());
		}

		validator.validate(journalpost);
		avbrytJournalpostUpdater.updateJournalpost(journalpost, domainRequest.getEndretAvNavn());
	}
}
