package no.nav.dokarkiv.core.consumer.aktoer;


import no.nav.dokarkiv.core.consumer.ConsumerRecoverableException;

/**
 * Exception PersonIkkeFunnetException.
 *
 * @author Tak Wai Wang (Capgemini)
 */
public class PersonIkkeFunnetException extends ConsumerRecoverableException {
	
	/** Serialization ID */
	private static final long serialVersionUID = -***gammelt_fnr***0010020L;

	/**
	 * Constructs a new PersonIkkeFunnetException.
	 *
	 * @param cause The exception cause.
	 * @param message The exception message.
	 */
	public PersonIkkeFunnetException(Throwable cause, String message) {
		super(cause, message);
	}

	
}
