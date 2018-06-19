package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;

/**
 * 
 * Interface for mapping a web service request to domain request for FerdigstillJournalpost
 * 
 * 
 * @author Stig Strøm
 *
 */
public interface FerdigstillJournalpostRequestMapper {
	
	/**
	 * Maps from web service request to domain request for FerdigstillJournalpost
	 * 
	 * 
	 * @param wsRequest the request from the web service
	 * @return the domain request
	 */
	FerdigstillJournalpostRequestTo map(FerdigstillJournalpostRequest wsRequest);
}
