package no.nav.dokarkiv.core.exceptions;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class FeilStrukturException extends FunctionalRecoverableException {

	public FeilStrukturException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public FeilStrukturException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}

}
