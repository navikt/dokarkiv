package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;


/**
 * Interface for the operation FjernFerdigstiltDokumentService
 *
 * @author Stig Strøm
 */
public interface FjernFerdigstiltDokumentService {

	/**
	 * Reverts a Ferdigstilt document back to a Redigerbart document
	 *
	 * @param domainRequest the domain request
	 * @throws NoJournalpostFoundException         when cannot find a journalpost in the input
	 * @throws NoDokumentInfoFoundException        when cannot find dokumentinfo in the input
	 * @throws UgyldigJournalStatusVerdiException  JournalStatus is not D
	 * @throws UgyldigDokumentStatusVerdiException Dokument is either in UNDER_REDIGERING or AVBRUTT status code
	 */
	void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequestTo domainRequest) throws NoJournalpostFoundException,
			NoDokumentInfoFoundException, UgyldigJournalStatusVerdiException, UgyldigDokumentStatusVerdiException;
}
