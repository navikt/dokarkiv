package no.nav.dokarkiv.core.stelvio;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Thrown to indicate that an unrecoverable exception has occured.
 *
 * This is the base exception that all unrecoverable exceptions must extend. Applications will typically not handle recovery
 * from exceptions of this type.
 *
 * @author Christian Kjendseth Wiik (Accenture)
 * @author Petter Skodvin
 */
public abstract class UnrecoverableException extends RuntimeException {

	private boolean handled;

	private long errorId;

	/**
	 * Constructs an <code>UnrecoverableException</code> with message and cause.
	 *
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	protected UnrecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an <code>UnrecoverableException</code> with message.
	 *
	 * @param message -
	 *            the exception message.
	 */
	protected UnrecoverableException(String message) {
		super(message);
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
		builder.append("handled", handled);

		return builder.toString();
	}

	/**
	 * This method was originally abstract, forcing subclasses to implement logic to return a message template (a String to be
	 * parameterized), which would be parameterized at runtime.
	 *
	 * However, this concept should no longer be used (error prone at runtime). The method is kept not to break backwards
	 * compatability.
	 *
	 * @param numArgs
	 *            the number of arguments (varargs) passed to the exception constructor.
	 * @return an empty string (originally the template to use for constructing the exception's message).
	 */
	protected String messageTemplate(final int numArgs) {
		return "";
	}

}