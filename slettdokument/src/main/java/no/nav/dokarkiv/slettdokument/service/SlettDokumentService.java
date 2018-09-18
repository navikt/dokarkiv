package no.nav.dokarkiv.slettdokument.service;

import no.nav.dokarkiv.slettdokument.SlettDokumentResponse;

/**
 * Interface for operation SlettDokument
 */
public interface SlettDokumentService {

	/**
	 * Sets slettet on a DokumentInfo to true
	 */
	SlettDokumentResponse slettDokument(SlettDokumentRequestTo domainRequest);
}
