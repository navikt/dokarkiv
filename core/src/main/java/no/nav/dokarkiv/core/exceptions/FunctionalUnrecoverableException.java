package no.nav.dokarkiv.core.exceptions;

/**
 * Base exception for exceptions considered functional and unrecoverable. Should be inherited by application exceptions in this
 * category.
 *
 * @author Christian Kjendseth Wiik (Accenture)
 * @author Stig Kleppe-Jørgensen
 */
public abstract class FunctionalUnrecoverableException extends UnrecoverableException {

	/**
	 * Constructs a <code>FunctionalUnrecoverableException</code> with message and cause.
	 *
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public FunctionalUnrecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>FunctionalUnrecoverableException</code> with message.
	 *
	 * @param message -
	 *            the exception message.
	 */
	public FunctionalUnrecoverableException(String message) {
		super(message);
	}
}