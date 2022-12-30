package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DefaultAvbrytJournalpostService implements AvbrytJournalpostService {

	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;
	private final AvbrytJournalpostUpdater avbrytJournalpostUpdater;
	private final AvbrytJournalpostValidator validator;

	public DefaultAvbrytJournalpostService(JournalpostRepositorySkjermet journalpostRepositorySkjermet, AvbrytJournalpostUpdater avbrytJournalpostUpdater, AvbrytJournalpostValidator validator) {
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
		this.avbrytJournalpostUpdater = avbrytJournalpostUpdater;
		this.validator = validator;
	}

	@Override
	public void avbrytJournalpost(AvbrytJournalpostRequestTo domainRequest) throws NoJournalpostFoundException, UgyldigJournalStatusOvergangException {
		Assert.notNull(domainRequest, "Request cannot be empty or missing");
		domainRequest.validate();

		Journalpost journalpost = journalpostRepositorySkjermet.findById(domainRequest.getJournalpostId())
				.orElseThrow(() -> new NoJournalpostFoundException("Journalpost with id: " + domainRequest.getJournalpostId() + " not found", domainRequest
						.getJournalpostId()));

		validator.validate(journalpost);
		avbrytJournalpostUpdater.updateJournalpost(journalpost, domainRequest.getEndretAvNavn());
	}
}
