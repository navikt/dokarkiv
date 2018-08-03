package no.nav.dokarkiv.core.dokumenturl;

import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Defines HentDokumentUrl operation.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public interface HentDokumentUrl {

	/**
	 * HentDokumentUrl.
	 * 
	 * @param hentDokumentUrlRequest
	 *            The request.
	 * @return The response.
	 * @throws NoJournalpostFoundException
	 *             NoJournalpostFoundException.
	 * @throws InvalidFilUuidException
	 *             InvalidFilUuidException.
	 */
	HentDokumentUrlResponse hentDokumentUrl(HentDokumentUrlRequest hentDokumentUrlRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException;

	HentDokumentUrlResponse hentDokumentUrlJoark(HentDokumentUrlRequest hentDokumentUrlRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException;

}
