package no.nav.dokarkiv.core.repository;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public interface JoarkCustomRepository {

	Long findJournalpostIdByTillegssopplysningKeyAndValue(String nokkel, String key);
}
