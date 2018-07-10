package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.core.domain.entities.Journalpost;

/**
 * Mapper for mapping between FIM behandleJournal Journalpost and internal
 * domain Journalpost.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface JournalpostMapper {

	/**
	 * Map Journalpost.
	 *
	 * @param wsJournalpost The ws Journalpost
	 * @return The domain Journalpost
	 */
	Journalpost map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost wsJournalpost);

}
