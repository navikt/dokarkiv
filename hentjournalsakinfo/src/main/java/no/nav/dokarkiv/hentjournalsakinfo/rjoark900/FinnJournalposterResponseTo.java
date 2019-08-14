package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class FinnJournalposterResponseTo {
	private final List<JournalpostDto> tilgangJournalposter;
}
