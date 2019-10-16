package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktBrukerResponseTo {
	private final List<JournalpostDto> journalposter;
}
