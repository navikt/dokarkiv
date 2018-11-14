package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class TilgangJournalpostBulkService {

	private static final List<FagomradeCode> PENSJON_ONLY = Arrays.asList(FagomradeCode.PEN, FagomradeCode.UFO);
	private final TilgangJournalpostBulkRepository tilgangJournalpostBulkRepository;

	@Inject
	public TilgangJournalpostBulkService(TilgangJournalpostBulkRepository tilgangJournalpostBulkRepository) {
		this.tilgangJournalpostBulkRepository = tilgangJournalpostBulkRepository;
	}

	public TilgangJournalpostBulkResponseTo tilgangJournalpostBulk(TilgangJournalpostBulkRequestTo tilgangJournalpostBulkRequestTo) {
		final List<TilgangJournalpostDto> tilgangJournalposter = new ArrayList<>();
		// TODO parallell
		if (!tilgangJournalpostBulkRequestTo.getGsakSakIds().isEmpty()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangJournalposter(tilgangJournalpostBulkRequestTo.getGsakSakIds(),
					Arkivsaksystem.GSAK,
					new TilgangJournalposterFilter(
							tilgangJournalpostBulkRequestTo.getFraDato(),
							tilgangJournalpostBulkRequestTo.getInkluderTema(),
							tilgangJournalpostBulkRequestTo.getInkluderJournalStatus(),
							tilgangJournalpostBulkRequestTo.getInkluderJournalpostType(),
							tilgangJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}

		if (!tilgangJournalpostBulkRequestTo.getPsakSakIds().isEmpty()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangJournalposter(tilgangJournalpostBulkRequestTo.getPsakSakIds(),
					Arkivsaksystem.PSAK,
					new TilgangJournalposterFilter(
							tilgangJournalpostBulkRequestTo.getFraDato(),
							tilgangJournalpostBulkRequestTo.getInkluderTema().stream().filter(t -> t == FagomradeCode.UFO || t == FagomradeCode.PEN).collect(Collectors.toList()),
							tilgangJournalpostBulkRequestTo.getInkluderJournalStatus(),
							tilgangJournalpostBulkRequestTo.getInkluderJournalpostType(),
							tilgangJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		if (tilgangJournalpostBulkRequestTo.isInkluderMidlertidigeJournalposter()) {
			List<TilgangJournalpostDto> journalposts = tilgangJournalpostBulkRepository.tilgangMidlertidigeJournalposter(tilgangJournalpostBulkRequestTo.getAlleIdenter(),
					new TilgangJournalposterFilter(
							tilgangJournalpostBulkRequestTo.getFraDato(),
							tilgangJournalpostBulkRequestTo.getInkluderTema(),
							tilgangJournalpostBulkRequestTo.getInkluderJournalStatus().stream().filter(js -> js == JournalStatusCode.M || js == JournalStatusCode.MO).collect(Collectors.toList()),
							tilgangJournalpostBulkRequestTo.getInkluderJournalpostType(),
							tilgangJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(TilgangJournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		return new TilgangJournalpostBulkResponseTo(tilgangJournalposter);
	}
}
