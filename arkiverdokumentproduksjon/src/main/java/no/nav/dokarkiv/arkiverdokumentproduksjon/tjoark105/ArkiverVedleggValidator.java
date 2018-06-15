package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.domain.dok.joark.Journalpost;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggRequestTo;

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
	 * @param journalpost The journalpost to be updated
	 * @param journalpostId The journalpostId
	 * @throws NoJournalpostFoundException
	 */
	void validate(Journalpost journalpost, Long journalpostId) throws NoJournalpostFoundException;

}
