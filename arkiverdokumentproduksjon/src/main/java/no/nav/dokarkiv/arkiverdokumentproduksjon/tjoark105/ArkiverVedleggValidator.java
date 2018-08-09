package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Interface for validating ArkiverVedlegg
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public interface ArkiverVedleggValidator {

	/**
	 * Validates arkiverVedlegg request mapped from ws-request
	 *
	 * @param arkiverVedleggRequestTo
	 */
	void validate(ArkiverVedleggRequestTo arkiverVedleggRequestTo);

	/**
	 * Validates that the journalpost exists and can be updated.
	 *
	 * @param journalpost   The journalpost to be updated
	 * @param journalpostId The journalpostId
	 * @throws NoJournalpostFoundException
	 */
	void validate(Journalpost journalpost, Long journalpostId) throws NoJournalpostFoundException;

}
