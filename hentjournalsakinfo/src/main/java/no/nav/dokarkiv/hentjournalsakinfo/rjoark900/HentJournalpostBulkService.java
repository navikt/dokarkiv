package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentJournalpostBulkService {

	private final HentJournalpostBulkRepository hentJournalpostBulkRepository;

	@Inject
	public HentJournalpostBulkService(HentJournalpostBulkRepository hentJournalpostBulkRepository) {
		this.hentJournalpostBulkRepository = hentJournalpostBulkRepository;
	}

	public HentJournalpostBulkResponseTo hentJournalpostBulk(HentJournalpostBulkRequestTo hentJournalpostBulkRequestTo) {
		final List<JournalpostDto> tilgangJournalposter = new ArrayList<>();
		// TODO parallell
		if (!hentJournalpostBulkRequestTo.getGsakSakIds().isEmpty()) {
			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.tilgangJournalposter(hentJournalpostBulkRequestTo.getGsakSakIds(),
					Arkivsaksystem.GSAK,
					new BulkJournalposterFilter(
							hentJournalpostBulkRequestTo.getFraDato(),
							hentJournalpostBulkRequestTo.getInkluderTema(),
							hentJournalpostBulkRequestTo.getInkluderJournalStatus(),
							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(JournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}

		if (!hentJournalpostBulkRequestTo.getPsakSakIds().isEmpty()) {
			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.tilgangJournalposter(hentJournalpostBulkRequestTo.getPsakSakIds(),
					Arkivsaksystem.PSAK,
					new BulkJournalposterFilter(
							hentJournalpostBulkRequestTo.getFraDato(),
							hentJournalpostBulkRequestTo.getInkluderTema().stream().filter(t -> t == FagomradeCode.UFO || t == FagomradeCode.PEN).collect(Collectors.toList()),
							hentJournalpostBulkRequestTo.getInkluderJournalStatus(),
							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(JournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		if (hentJournalpostBulkRequestTo.isInkluderMidlertidigeJournalposter()) {
			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.tilgangMidlertidigeJournalposter(hentJournalpostBulkRequestTo.getAlleIdenter(),
					new BulkJournalposterFilter(
							hentJournalpostBulkRequestTo.getFraDato(),
							hentJournalpostBulkRequestTo.getInkluderTema(),
							hentJournalpostBulkRequestTo.getInkluderJournalStatus().stream().filter(js -> js == JournalStatusCode.M || js == JournalStatusCode.MO).collect(Collectors.toList()),
							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
					)
			).stream().map(JournalpostDto::new).collect(Collectors.toList());
			tilgangJournalposter.addAll(journalposts);
		}
		return new HentJournalpostBulkResponseTo(tilgangJournalposter);
	}
}
