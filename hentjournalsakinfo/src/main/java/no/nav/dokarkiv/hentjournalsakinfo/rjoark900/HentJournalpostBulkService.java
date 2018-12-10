package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentJournalpostBulkService {
	private final FinnJournalposterSpringJdbcRepository finnJournalposterSpringJdbcRepository;

	@Inject
	public HentJournalpostBulkService(FinnJournalposterSpringJdbcRepository finnJournalposterSpringJdbcRepository) {
		this.finnJournalposterSpringJdbcRepository = finnJournalposterSpringJdbcRepository;
	}

	public FinnJournalposterResponseTo hentJournalpostBulk(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		List<JournalpostDto> journalpostDtos = finnJournalposterSpringJdbcRepository.hentJournalposter(
				finnJournalposterRequestTo.getGsakSakIds(),
				finnJournalposterRequestTo.getPsakSakIds(),
				new JournalpostFilter(finnJournalposterRequestTo)
		);
		return new FinnJournalposterResponseTo(journalpostDtos);
	}
}
