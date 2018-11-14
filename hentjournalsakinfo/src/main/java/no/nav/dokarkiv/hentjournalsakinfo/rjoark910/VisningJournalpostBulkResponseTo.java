package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class VisningJournalpostBulkResponseTo {
	private final List<JournalpostDto> journalposter;
}
