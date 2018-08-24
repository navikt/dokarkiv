package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class AlleredeFerdigstiltException extends DokarkivFunctionalException {

	public AlleredeFerdigstiltException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public AlleredeFerdigstiltException(String message, Throwable cause, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
