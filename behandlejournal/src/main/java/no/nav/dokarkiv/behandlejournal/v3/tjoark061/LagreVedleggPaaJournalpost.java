package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Service to add a new vedlegg to an already persisted Journalpost.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public interface LagreVedleggPaaJournalpost {

	/**
	 * Adds a vedlegg to an already stored Journalpost.
	 * 
	 * @param lagreVedleggPaaJournalpostRequest
	 *            request with vedlegg to add
	 * @return LagreVedleggPaaJournalpostResponse containing the dokumentId of
	 *         the stored vedlegg.
	 * @throws NoJournalpostFoundException
	 */
	LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(
			LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest) throws NoJournalpostFoundException;
}
