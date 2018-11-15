package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilgangJournalpostDto;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentTilgangJournalpostResponse {
	TilgangJournalpostDto tilgangJournalpostDto;
}
