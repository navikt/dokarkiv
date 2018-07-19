package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;

/**
 * Mapper between {@link IdentifiserJournalpostRequest} and {@link IdentifiserJournalpostToRequest}.
 *
 * @author Ketill Fenne, Visma Consulting AS
 */
public class IdentifiserJournalpostV2RequestMapper {

	public IdentifiserJournalpostToRequest map(IdentifiserJournalpostRequest request) {
		return IdentifiserJournalpostToRequest.builder()
				.kanalReferanseId(request.getKanalReferanseId())
				.mottaksKanal(request.getMottakskanal() == null ? null : MottaksKanalCode.valueOf(request.getMottakskanal()))
				.build();
	}
}
