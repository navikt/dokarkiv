package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TilgangJournalpostBulkService {

	private final TilgangJournalpostBulkRepository tilgangJournalpostBulkRepository;

	@Inject
	public TilgangJournalpostBulkService(TilgangJournalpostBulkRepository tilgangJournalpostBulkRepository) {
		this.tilgangJournalpostBulkRepository = tilgangJournalpostBulkRepository;
	}

	public TilgangJournalpostBulkResponseTo tilgangJournalpostBulk(TilgangJournalpostBulkRequestTo tilgangJournalpostBulkRequestTo) {
		final List<TilgangJournalpostDto> tilgangJournalposter = new ArrayList<>();
		final TilgangJournalposterFilter tilgangJournalposterFilter = new TilgangJournalposterFilter(
				tilgangJournalpostBulkRequestTo.getFraDato(),
				tilgangJournalpostBulkRequestTo.getInkluderTema(),
				tilgangJournalpostBulkRequestTo.getInkluderJournalStatus(),
				tilgangJournalpostBulkRequestTo.getInkluderJournalpostType(),
				tilgangJournalpostBulkRequestTo.isVisFeilregistrerte()
		);
		// TODO parallell
		if (!tilgangJournalpostBulkRequestTo.getGsakSakIds().isEmpty()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangJournalposter(tilgangJournalpostBulkRequestTo.getGsakSakIds(),
					Arkivsaksystem.GSAK,
					tilgangJournalposterFilter
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}

		if (!tilgangJournalpostBulkRequestTo.getPsakSakIds().isEmpty()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangJournalposter(tilgangJournalpostBulkRequestTo.getGsakSakIds(),
					Arkivsaksystem.PSAK,
					tilgangJournalposterFilter
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		if(tilgangJournalpostBulkRequestTo.isInkluderMidlertidigeJournalposter()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangMidlertidigeJournalposter(tilgangJournalpostBulkRequestTo.getAlleIdenter(),
					new TilgangMidlertidigeJournalposterFilter(
							tilgangJournalpostBulkRequestTo.getFraDato(),
							tilgangJournalpostBulkRequestTo.getInkluderTema(),
							tilgangJournalpostBulkRequestTo.getInkluderJournalpostType()
					)
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		return new TilgangJournalpostBulkResponseTo(tilgangJournalposter);
	}
}
