package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class FinnJournalposterStatusResponseTo {
	private final List<JournalpostDto> tilgangJournalposter;
}
