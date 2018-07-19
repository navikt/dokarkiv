package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;

/**
 * Transport object for {@link IdentifiserJournalpostService }
 *
 * @author Ketill Fenne, Visma Consulting
 */
@Data
@Builder
public class IdentifiserJournalpostToRequest {
	private final String kanalReferanseId;
	private final MottaksKanalCode mottaksKanal;
}
