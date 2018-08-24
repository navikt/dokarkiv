package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class UgyldigInputException extends DokarkivFunctionalException {

	public UgyldigInputException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public UgyldigInputException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

	public UgyldigInputException(String message) {
		super(message);
	}
}
