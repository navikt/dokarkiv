package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.List;

/**
 * Interface for the innsyn operation HentMinTilgjeneligeJournalpostListe (TJOARK053)
 *
 * @author Torgeir Cook, Visma Consulting
 */
public interface HentMinTilgjengeligeJournalpostListeService {

	/**
	 * Filters journalposts tied to Saks in input
	 *
	 * @param hentJournalpostListeToRequest
	 * @return List of filtered journalposts
	 */
	List<Journalpost> hentMineTilgjengeligeJournalposter(HentJournalpostListeToRequest hentJournalpostListeToRequest);
}
