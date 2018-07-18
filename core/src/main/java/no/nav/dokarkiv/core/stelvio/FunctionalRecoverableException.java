package no.nav.dokarkiv.core.stelvio;

/**
 * Base exception for exceptions considered functional and recoverable. Should
 * be inherited by application exceptions in this category.
 *
 * @author Christian Kjendseth Wiik (Accenture)
 * @author Stig Kleppe-Jørgensen
 * @deprecated forenkle dette. Kun bruke DokarkivFunctionalException
 */
@Deprecated
public abstract class FunctionalRecoverableException extends RecoverableException {
	/**
	 * Constructs a <code>FunctionalRecoverableException</code> with message and cause.
	 * 
	 * @param message - the exception message.
	 * @param cause - the throwable that caused the exception to be raised.
	 */
	public FunctionalRecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>FunctionalRecoverableException</code> with message.
	 * 
	 * @param message - the exception message.
	 */	
	public FunctionalRecoverableException(String message) {
		super(message);
	}
	
}