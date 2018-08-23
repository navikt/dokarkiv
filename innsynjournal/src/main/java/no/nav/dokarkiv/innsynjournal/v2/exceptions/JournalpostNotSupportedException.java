package no.nav.dokarkiv.innsynjournal.v2.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * Exception indicating that the selected Journalpost is not supported for the
 * current operation.
 * 
 * @author A137939 - Per Abich, Visma Consulting
 * 
 */
public class JournalpostNotSupportedException extends DokarkivFunctionalException {

	/**
	 * Constructs a {@link JournalpostNotSupportedException} with message and
	 * cause.
	 * 
	 * @param message
	 * @param cause
	 */
	public JournalpostNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a {@link JournalpostNotSupportedException} with message
	 * 
	 * @param message
	 */
	public JournalpostNotSupportedException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = ***gammelt_fnr***1620137L;

}
