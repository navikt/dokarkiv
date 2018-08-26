package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class FeilStrukturException extends DokarkivFunctionalException {

	public FeilStrukturException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public FeilStrukturException(String message, Throwable cause, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
