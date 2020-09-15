package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown by Journalbehandling operations when the input Journalpost has an
 * invalid structure.
 * 
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class InvalidJournalpostStructureException extends DokarkivFunctionalException {

	/** Serialization UID */
	private static final long serialVersionUID = 4800123366475497536L;

	/**
	 * Constructs a new InvalidJournalpostStructureException.
	 *
	 * @param message The exception message.
	 */
	public InvalidJournalpostStructureException(String message) {
		super(message);
	}

}
