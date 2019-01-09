package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class FinnJournalposterService {
	private final FinnJournalposterSpringJdbcRepository finnJournalposterSpringJdbcRepository;

	@Inject
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
