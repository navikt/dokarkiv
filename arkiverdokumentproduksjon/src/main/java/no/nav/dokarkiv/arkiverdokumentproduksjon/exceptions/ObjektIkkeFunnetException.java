package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class ObjektIkkeFunnetException extends FunctionalRecoverableException {

	public ObjektIkkeFunnetException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public ObjektIkkeFunnetException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
