package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when validating GjelderInfo.
 * 
 * @author Hans Olav Loftum, BEKK
 */
public class InvalidBrukerException extends FunctionalUnrecoverableException{

	/** Serialization ID */
	private static final long serialVersionUID = -***gammelt_fnr***71491890L;
	
	/**
	 * Constructs a new InvalidBrukerException.
	 *
	 * @param message The Exception message.
	 */
	public InvalidBrukerException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a new InvalidBrukerException.
	 *
	 * @param message The Exception message.
	 * @param cause The Exception cause.
	 */
	public InvalidBrukerException(String message, Throwable cause) {
		super(message, cause);
	}
}
