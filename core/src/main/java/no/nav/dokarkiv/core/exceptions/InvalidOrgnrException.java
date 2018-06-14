package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when validating OrgNr.
 * 
 * @author Hans Olav Loftum, BEKK
 */
public class InvalidOrgnrException extends FunctionalUnrecoverableException {

	/** Serialization ID */
	private static final long serialVersionUID = ***gammelt_fnr***41658110L;
	
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
