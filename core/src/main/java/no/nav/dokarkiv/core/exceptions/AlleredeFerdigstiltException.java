package no.nav.service.dok.joark.nsb.exceptions;

import no.stelvio.common.error.FunctionalRecoverableException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class AlleredeFerdigstiltException extends FunctionalRecoverableException {

	public AlleredeFerdigstiltException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public AlleredeFerdigstiltException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
