package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import org.springframework.stereotype.Service;

@Service
public class SafHentJournalpostService {

	private final HentJournalpostSpringJdbcRepository hentJournalpostSpringJdbcRepository;

	public SafHentJournalpostService(HentJournalpostSpringJdbcRepository hentJournalpostSpringJdbcRepository) {
		this.hentJournalpostSpringJdbcRepository = hentJournalpostSpringJdbcRepository;
	}

	public SafHentJournalpostResponse hentJournalpostByJournalpostId(Long journalpostId) {
		return new SafHentJournalpostResponse(hentJournalpostSpringJdbcRepository.finnJournalposter(journalpostId.toString()));
	}
}
