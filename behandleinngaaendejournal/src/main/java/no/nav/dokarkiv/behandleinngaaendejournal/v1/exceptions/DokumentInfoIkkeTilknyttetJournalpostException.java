package no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting. 24.05.2017.
 */
public class DokumentInfoIkkeTilknyttetJournalpostException extends DokarkivFunctionalException {
	
	public DokumentInfoIkkeTilknyttetJournalpostException() {
		super();
	}
	
	public DokumentInfoIkkeTilknyttetJournalpostException(String message) {
		super(message);
	}
	
}