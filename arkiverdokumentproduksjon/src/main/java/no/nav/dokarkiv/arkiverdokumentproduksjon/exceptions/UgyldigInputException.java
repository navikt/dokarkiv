package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class UgyldigInputException extends FunctionalRecoverableException {

	public UgyldigInputException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public UgyldigInputException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
