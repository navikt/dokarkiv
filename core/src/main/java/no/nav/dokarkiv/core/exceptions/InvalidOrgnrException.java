package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when validating OrgNr.
 * 
 * @author Hans Olav Loftum, BEKK
 */
public class InvalidOrgnrException extends DokarkivFunctionalException {

	/** Serialization ID */
	private static final long serialVersionUID = 4781594055341658110L;
	
	/**
	 * Constructs a new InvalidOrgnrException.
	 *
	 * @param message The Exception message.
	 */
	public InvalidOrgnrException(String message) {
		super(message);
	}

	/**
	 * Constructs a new InvalidOrgnrException.
	 *
	 * @param message The Exception message.
	 * @param cause The Exception cause.
	 */
	public InvalidOrgnrException(String message, Throwable cause) {
		super(message, cause);
	}
}
