package no.nav.dokarkiv.core.exceptions;

import no.stelvio.common.error.FunctionalRecoverableException;

/**
 * @author Leo-Andreas Ervik, Visma Consulting
 */
public class KanIkkeFerdigstillesException extends FunctionalRecoverableException {

	public KanIkkeFerdigstillesException(String message, Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId));
	}

	public KanIkkeFerdigstillesException(String message, Throwable cause , Long journalpostId) {
		super(message + (". journalpostId=" + journalpostId), cause);
	}
}
