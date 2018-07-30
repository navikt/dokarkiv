package no.nav.dokarkiv.journal.v3.tjoark058;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.List;

@Data
@Builder
public class HentKjerneJournalpostListeResponseTo {
	private List<Journalpost> journalpostListe;
	private boolean sisteIntervall;
}
