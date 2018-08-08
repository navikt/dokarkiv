package no.nav.dokarkiv.core.dokument;

import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.journal.JournalServiceBi;

/**
 * Defines the contract for the 'Hent dokument' operation.
 *
 * @author Carl-Henrik Wolf Lund
 */
public interface HentDokument {

	/**
	 * Operation for retrieving documents.
	 *
	 * @param hentDokumentRequest The request.
	 * @return The response.
	 * @throws NoJournalpostFoundException NoJournalpostFoundException.
	 * @throws InvalidFilUuidException     InvalidFilUuidException.
	 * @see {@link JournalServiceBi#hentDokument(HentDokumentRequest)}
	 */
	HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException;

}
