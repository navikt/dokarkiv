package no.nav.dokarkiv.core.dokument;

import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
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
	 * @see {@link JournalServiceBi#hentDokument(HentDokumentRequest)}
	 * 
	 * @param hentDokumentRequest The request.
	 * @return The response.
	 * @throws NoJournalpostFoundException NoJournalpostFoundException.
	 * @throws InvalidFilUuidException InvalidFilUuidException.
	 * @throws DocumentNotFoundException DocumentNotFoundException
	 */
	HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException;

}
