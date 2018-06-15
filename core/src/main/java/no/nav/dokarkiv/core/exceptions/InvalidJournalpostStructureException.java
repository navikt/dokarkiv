package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Thrown by Journalbehandling operations when the input Journalpost has an
 * invalid structure.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class InvalidJournalpostStructureException extends FunctionalUnrecoverableException {

	/** Serialization UID */
	private static final long serialVersionUID = ***gammelt_fnr***75497536L;

	/**
	 * Constructs a new InvalidJournalpostStructureException.
	 *
	 * @param message The exception message.
	 */
	public InvalidJournalpostStructureException(String message) {
		super(message);
	}

}
