package no.nav.dokarkiv.core.stelvio;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Base exception for exceptions considered system and unrecoverable. Should be inherited by application exceptions in this
 * category.
 * 
 * @author Morten Andersen-Gott
 * @author Christian Kjendseth Wiik (Accenture)
 * @author Stig Kleppe-Jørgensen
 */
@Deprecated
public abstract class SystemUnrecoverableException extends UnrecoverableException {

	private ErrorCode errorCode;

	/**
	 * Constructs a <code>SystemUnrecoverableException</code> with message, error code and cause.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param errorCode -
	 *            the error code for the exception.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public SystemUnrecoverableException(String message, ErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	/**
	 * Constructs a <code>SystemUnrecoverableException</code> with message and error code.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param errorCode -
	 *            the error code for the exception.
	 */
	public SystemUnrecoverableException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Constructs a <code>SystemUnrecoverableException</code> with message and cause.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public SystemUnrecoverableException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>SystemUnrecoverableException</code> with message.
	 * 
	 * @param message -
	 *            the exception message.
	 */
	public SystemUnrecoverableException(String message) {
		super(message);
	}

	/**
	 * Get the operational errorcode associated with this exception instance.
	 * 
	 * @return errorCode
	 */
	public final ErrorCode getErrorCode() {
		return errorCode;
	}

	/**
	 * Set the operational errorcode associated with this exception instance.
	 * 
	 * @param errorCode
	 *            to set
	 */
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Returns a String representation of object properties.
	 * 
	 * @return String representation of object properties.
	 */
	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("errorCode", getErrorCode());

		return builder.toString();
	}

}