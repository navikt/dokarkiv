package no.nav.dokarkiv.journalpost.v1.rjoark202;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.rjoark202.util.JournalpostMapper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(value = "opprettNyJournalpostService")
public class OpprettJournalpostService {

	private final JoarkRepository joarkRepository;
	private final JournalpostMapper journalpostMapper;

	@Inject
	public OpprettJournalpostService(final JoarkRepository joarkRepository) {
		this.joarkRepository = joarkRepository;
		this.journalpostMapper = new JournalpostMapper();
	}

	public Long opprettJournalpost(OpprettJournalpostRequest request) {

		Journalpost journalpost = journalpostMapper.map(request);

		// validering ??

		joarkRepository.save(journalpost);

		return journalpost.getJournalpostId();
	}
}
