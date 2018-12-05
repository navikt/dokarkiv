package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentJournalpostBulkService {
	private final HentJournalpostBulkRepository hentJournalpostBulkRepository;
	private final HentJournalpostBulkSpringJdbcRepository hentJournalpostBulkSpringJdbcRepository;

	@Inject
	public HentJournalpostBulkService(HentJournalpostBulkRepository hentJournalpostBulkRepository,
									  HentJournalpostBulkSpringJdbcRepository hentJournalpostBulkSpringJdbcRepository) {
		this.hentJournalpostBulkRepository = hentJournalpostBulkRepository;
		this.hentJournalpostBulkSpringJdbcRepository = hentJournalpostBulkSpringJdbcRepository;
	}

	public HentJournalpostBulkResponseTo hentJournalpostBulk(HentJournalpostBulkRequestTo hentJournalpostBulkRequestTo) {
		List<JournalpostDto> journalpostDtos = hentJournalpostBulkSpringJdbcRepository.hentJournalposter(
				hentJournalpostBulkRequestTo.getGsakSakIds(),
				hentJournalpostBulkRequestTo.getPsakSakIds(),
				new BulkJournalposterFilter(hentJournalpostBulkRequestTo)
		);
		return new HentJournalpostBulkResponseTo(journalpostDtos);
	}
}
