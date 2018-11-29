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
		List<JournalpostDto> tilgangJournalposter = hentJournalpostBulkSpringJdbcRepository.hentJournalposter(hentJournalpostBulkRequestTo.getGsakSakIds(),
				hentJournalpostBulkRequestTo.getPsakSakIds(),
				new BulkJournalposterFilter(
						hentJournalpostBulkRequestTo.getFraDato(),
						hentJournalpostBulkRequestTo.getInkluderTema(),
						hentJournalpostBulkRequestTo.getInkluderJournalStatus(),
						hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
						hentJournalpostBulkRequestTo.isVisFeilregistrerte()
				)
		);
//		final List<JournalpostDto> tilgangJournalposter = new ArrayList<>();
//		// TODO jdbc sql unions
//		if (!hentJournalpostBulkRequestTo.getGsakSakIds().isEmpty()) {
//			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.hentJournalposter(hentJournalpostBulkRequestTo.getGsakSakIds(),
//					Arkivsaksystem.GSAK,
//					new BulkJournalposterFilter(
//							hentJournalpostBulkRequestTo.getFraDato(),
//							new ArrayList<>(),
//							hentJournalpostBulkRequestTo.getInkluderJournalStatus(),
//							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
//							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
//					)
//			).stream().map(JournalpostDto::new).collect(Collectors.toList());
//			tilgangJournalposter.addAll(journalposts);
//		}
//
//		if (!hentJournalpostBulkRequestTo.getPsakSakIds().isEmpty()) {
//			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.hentJournalposter(hentJournalpostBulkRequestTo.getPsakSakIds(),
//					Arkivsaksystem.PSAK,
//					new BulkJournalposterFilter(
//							hentJournalpostBulkRequestTo.getFraDato(),
//							new ArrayList<>(),
//							hentJournalpostBulkRequestTo.getInkluderJournalStatus(),
//							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
//							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
//					)
//			).stream().map(JournalpostDto::new).collect(Collectors.toList());
//			tilgangJournalposter.addAll(journalposts);
//		}
//		if (hentJournalpostBulkRequestTo.isInkluderMidlertidigeJournalposter()) {
//			List<JournalpostDto> journalposts = hentJournalpostBulkRepository.hentMidlertidigeJournalposter(hentJournalpostBulkRequestTo.getAlleIdenter(),
//					new BulkJournalposterFilter(
//							hentJournalpostBulkRequestTo.getFraDato(),
//							hentJournalpostBulkRequestTo.getInkluderTema(),
//							hentJournalpostBulkRequestTo.getInkluderJournalStatus().stream().filter(js -> js == JournalStatusCode.M || js == JournalStatusCode.MO).collect(Collectors.toList()),
//							hentJournalpostBulkRequestTo.getInkluderJournalpostType(),
//							hentJournalpostBulkRequestTo.isVisFeilregistrerte()
//					)
//			).stream().map(JournalpostDto::new).collect(Collectors.toList());
//			tilgangJournalposter.addAll(journalposts);
//		}
		return new HentJournalpostBulkResponseTo(tilgangJournalposter);
	}
}
