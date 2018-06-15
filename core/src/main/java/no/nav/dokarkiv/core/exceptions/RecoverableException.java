package no.nav.dokarkiv.core.exceptions;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Thrown to indicate that a recoverable exception in the application logic occured.
 * 
 * This is the base exception that all recoverable application specific exceptions must extend. That is, all clients that can
 * receive this type of exception should be able to recover from or at least handle it in a meaningful way.
 * 
 * @author Morten Andersen-Gott
 * @author Christian Kjendseth Wiik (Accenture)
 * @author Petter Skodvin, Accenture
 */
public abstract class RecoverableException extends Exception {

	private boolean logged;

	private boolean handled;

	private long errorId;

	/**
	 * Constructs a <code>RecoverableException</code> with message, error code and cause.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	protected RecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>RecoverableException</code> with message.
	 * 
	 * @param message -
	 *            the exception message.
	 */
	protected RecoverableException(String message) {
		super(message);
	}

	/** {@inheritDoc} */
	public final boolean isLogged() {
		return logged;
	}

	/** {@inheritDoc} */
	public final void setLogged() {
		logged = true;
	}

	/** {@inheritDoc} */
	public final long getErrorId() {
		return errorId;
	}

	/** {@inheritDoc} */
	public boolean isHandled() {
		return handled;
	}

	/** {@inheritDoc} */
	public void setHandled() {
		handled = true;
	}

	/**
	 * Returns a String representation of object properties.
	 * 
	 * @return String representation of object properties.
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);

		builder.append("message", getMessage());
		builder.append("logged", logged);
		builder.append("handled", handled);

		return builder.toString();
	}

}