package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * Implementation of the AvbrytJournalpostService
 *
 * @author Stig Strøm
 */
@Component
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

		Journalpost journalpost = joarkRepository.findById(domainRequest.getJournalpostId())
				.orElseThrow(() -> new NoJournalpostFoundException("Journalpost with id: " + domainRequest.getJournalpostId() + " not found", domainRequest.getJournalpostId()));

		validator.validate(journalpost);
		avbrytJournalpostUpdater.updateJournalpost(journalpost, domainRequest.getEndretAvNavn());
	}
}
