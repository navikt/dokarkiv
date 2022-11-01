package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.hentjournalsakinfo.JournalpostFilter;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinnJournalposterService {
	private final FinnJournalposterSpringJdbcRepository finnJournalposterSpringJdbcRepository;

	public FinnJournalposterService(FinnJournalposterSpringJdbcRepository finnJournalposterSpringJdbcRepository) {
		this.finnJournalposterSpringJdbcRepository = finnJournalposterSpringJdbcRepository;
	}

	public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		List<JournalpostDto> journalpostDtos = finnJournalposterSpringJdbcRepository.finnJournalposter(
				finnJournalposterRequestTo.getGsakSakIds(),
				finnJournalposterRequestTo.getPsakSakIds(),
				new JournalpostFilter(finnJournalposterRequestTo)
		);
		return new FinnJournalposterResponseTo(journalpostDtos);
	}
}
