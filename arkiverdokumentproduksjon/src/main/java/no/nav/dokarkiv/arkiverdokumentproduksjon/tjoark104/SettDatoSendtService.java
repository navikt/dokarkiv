package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.service.dok.joark.nsb.to.SettDatoSendtRequestTo;

/**
 * Service thats sets sendtPrintDato on Journalposts
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface SettDatoSendtService {

	/**
	 * Set sendtPrintDato on the Journalposts given in the request
	 *
	 * @param domainRequest the request containing Journalpost references
	 */
	void settDatoSendt(SettDatoSendtRequestTo domainRequest);
}
