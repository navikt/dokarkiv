package no.nav.dokarkiv.core.journal;

import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoResponse;

/**
 * Defines the Joark information service Journal.
 * 
 * @author Magnus Skuland, Sirius IT
 * @author Rune Romundstad, Sirius IT
 */
public interface JournalServiceBi {

//	/**
//	 * Retrieves a <code>Journalpost</code> with all relations. This means
//	 * that all attributes are retrieved from all objects in the domain model,
//	 * e.g. <code>Saksrelasjon</code>, <code>Bruker</code>,
//	 * <code>DokumentInfo</code>.
//	 *
//	 * <p>
//	 * The following runtime exceptions may be thrown by the service:
//	 * <dl>
//	 * <dt><code>MultipleJournalpostEntriesException</code></dt>
//	 * <dd>If more than one entry is retrieved for a given journalpostId.</dd>
//	 * </dl>
//	 * </p>
//	 *
//	 * @param hentJournalpostRequest
//	 *            The request object.
//	 * @return A response containing a Journalpost with all relations,
//	 *         <code>null</code> if no persistent instance could be found for
//	 *         the given id.
//	 * @throws NoJournalpostFoundException
//	 *             If no journalpost could be found for given request.
//	 */
//	HentJournalpostResponse hentJournalpost(HentJournalpostRequest hentJournalpostRequest) throws NoJournalpostFoundException;

//	/**
//	 * Search for Journalposts based on different criterias. The search is split into 3 functions,
//	 * based on the values set in the <code>FinnJournalpostListeRequest</code>. For each search function
//	 * at least one parameter is required.
//	 *
//	 * @param finnJournalpostRequest
//	 * 				the request object containing the search parameters.
//	 * @return A response containing a list of Journalposts.
//	 */
//	FinnJournalpostResponse finnJournalpost(FinnJournalpostRequest finnJournalpostRequest);

	/**
	 * Retrieve a given document by journalpostId and filUuid.
	 * 
	 * @param hentDokumentRequest
	 *            Request-object with the journalpostID and the file type wanted
	 * @return hentDokumentResponse Response that holds the actual document
	 * @throws NoJournalpostFoundException
	 *             Thrown if the journalpost specified could not be found
	 * @throws InvalidFilUuidException
	 *             Thrown if either a FilDetaljer or DokumentFil cannot be found
	 *             by filuuid.
	 */
	HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException;

//	/**
//	 * Retrieves a URL to a document.
//	 *
//	 * @param hentDokumentUrlRequest
//	 *            The request containing a JournalpostId and filUuid.
//	 * @return A URL pointing to the document associated with the JournalpostId.
//	 * @throws NoJournalpostFoundException
//	 *             If no journalpost exists with the given journalpostId.
//	 * @throws InvalidFilUuidException
//	 *             If no FilDetaljer or DokumentFil is fi\ound for the given
//	 *             filUuid.
//	 */
//	HentDokumentUrlResponse hentDokumentUrl(HentDokumentUrlRequest hentDokumentUrlRequest) throws NoJournalpostFoundException,
//			InvalidFilUuidException;

	/**
	 * Retrieves an DokumentUrlInfo entry.
	 * 
	 * <p>
	 * This service throws the following unchecked exceptions: <br>
	 * <ul>
	 * <li>UrlNotValidException</li>
	 * <li>DokumentUrlNotFoundException</li>
	 * <li>MultipleDokumentUrlFoundException</li>
	 * </ul>
	 * 
	 * @param hentDokumentUrlInfoRequest
	 *            the request containing a docToken.
	 * @return An URL pointing to the document associated with the docToken.
	 */
	HentDokumentUrlInfoResponse hentDokumentUrlInfo(HentDokumentUrlInfoRequest hentDokumentUrlInfoRequest);
	
	
//	/**
//	 * Retrieves a brevgruppe based on a brevkode.
//	 *
//	 * @param identifiserBrevgruppeRequest The brevkode to lookup.
//	 * @return The brevgruppe matching the brevkode in the request.
//	 */
//	IdentifiserBrevgruppeResponse identifiserBrevgruppe(IdentifiserBrevgruppeRequest identifiserBrevgruppeRequest);
}
