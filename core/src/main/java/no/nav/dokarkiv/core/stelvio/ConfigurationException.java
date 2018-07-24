package no.nav.dokarkiv.core.stelvio;

/**
 * Subclasses of this exception is used to signal erroneous configuration.
 * 
 * @author Morten Andersen-Gott, Accenture
 */
public abstract class ConfigurationException extends FunctionalUnrecoverableException {

	/**
	 * Constructs a <code>ConfigurationException</code> with message and cause.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>ConfigurationException</code> with message.
	 * 
	 * @param message -
	 *            the exception message.
	 */
	public ConfigurationException(String message) {
		super(message);
	}

}