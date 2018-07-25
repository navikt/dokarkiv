package no.nav.dokarkiv.inngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting. 24.05.2017.
 */
public class JournalpostIkkeMidlertidigException extends DokarkivFunctionalException {
	
	public JournalpostIkkeMidlertidigException() {
		super();
	}
	
	public JournalpostIkkeMidlertidigException(String message) {
		super(message);
	}
	
}
