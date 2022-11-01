package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;

import java.util.List;

@Value
public class FinnJournalposterResponseTo {
	List<JournalpostDto> tilgangJournalposter;
}
