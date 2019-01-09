package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentTilgangJournalpostResponse {
	TilgangJournalpostDto tilgangJournalpostDto;
}
