package no.nav.dokarkiv.journal.v3.tjoark050;

import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;

/**
 * Defines operations that use existing HentDokumentUrl functionality with different input.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface HentDokumentUrlService {

	/**
	 * Get the URL of a document
	 * 
	 * @param hentDokumentUrlRequest The request
	 * @return The response with the URL
	 * @throws DocumentNotFoundException If the Journalpost og document is not found
	 */
	HentDokumentUrlResponseTo hentDokumentUrl(HentDokumentUrlRequestTo hentDokumentUrlRequest) throws DocumentNotFoundException;
	
}
