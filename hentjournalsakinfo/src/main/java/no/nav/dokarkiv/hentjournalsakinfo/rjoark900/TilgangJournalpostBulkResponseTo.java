package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangJournalpostBulkResponseTo {
	private final List<TilgangJournalpostDto> tilgangJournalposter;
}
