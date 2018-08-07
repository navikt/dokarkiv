package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;

/**
 * Service operation to retrieve Journalpost and DokumentInfo status and metaForceInstanceId if present.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface HentJournalOgDokumentStatus {
	
	/**
	 * Retrieve JournalpostStatus, DokumentInfoStatus and metaForceInstanceId
	 * 
	 * @param request The request with JournalpostId and DokumentInfoId
	 * @return The response with statuses and optionally metaForceInstanceId.
	 * @throws NoJournalpostFoundException If Journalpost is not found
	 * @throws NoDokumentInfoFoundException  If DokumentInfo is not found on Journalpost
	 */
	HentJournalOgDokumentStatusResponseTo hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequestTo request)
			throws NoJournalpostFoundException, NoDokumentInfoFoundException;

}
