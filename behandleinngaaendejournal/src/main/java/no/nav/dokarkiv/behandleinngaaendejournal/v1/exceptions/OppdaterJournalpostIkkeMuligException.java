package no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting. 31.05.2017.
 */
public class OppdaterJournalpostIkkeMuligException extends DokarkivFunctionalException {
	
	public OppdaterJournalpostIkkeMuligException(String message) {
		super(message);
	}
	
	public OppdaterJournalpostIkkeMuligException(String message, Throwable cause) {
		super(message, cause);
	}
}
