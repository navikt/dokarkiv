package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Transport object for {@link HentMinTilgjengeligeJournalpostListeService }
 *
 * @author Torgeir Cook, Visma Consulting
 */
@Data
@Builder
public class HentJournalpostListeToRequest {
	private final boolean merkInnsynDokument;
	private final List<SakFagsystem> saksListe = new ArrayList<>();
}
