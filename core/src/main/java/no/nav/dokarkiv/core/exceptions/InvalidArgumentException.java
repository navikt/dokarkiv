package no.nav.dokarkiv.core.exceptions;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Thrown to indicate that a method has been passed an invalid/illegal or inappropriate argument.
 *
 * @author Stig Kleppe-Jørgensen
 * @author Mette Lafton
 */
public class InvalidArgumentException extends DokarkivFunctionalException {
	private static final long serialVersionUID = 123345612346L;

	/* The name of the argument that is not used correctly */
	private final String argumentName;

	/* The value of the argument that is not used correctly */
	private final Object argumentValue;

	/**
	 * Constructs an <code>InvalidArgumentException</code> with message.
	 *
	 * @param message -
	 *            the exception message.
	 */
	public InvalidArgumentException(String message) {
		super(message);
		argumentName = null;
		argumentValue = null;
	}

	/**
	 * Constructs an <code>InvalidArgumentException</code> with message and cause.
	 *
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public InvalidArgumentException(String message, Throwable cause) {
		super(message, cause);
		argumentName = null;
		argumentValue = null;
	}

	/**
	 * Constructs an <code>InvalidArgumentException</code> with message, argument name and argument value.
	 *
	 * @param message -
	 *            the exception message.
	 * @param argumentName -
	 *            the name of the argument that is not used correctly.
	 * @param argumentValue -
	 *            the value of the argument that is not used correctly.
	 */
	public InvalidArgumentException(String message, String argumentName, Object argumentValue) {
		this(message, argumentName, argumentValue, null);
	}

	/**
	 * Constructs an <code>InvalidArgumentException</code> with message, argument name, argument value and cause.
	 *
	 * @param message -
	 *            the exception message.
	 * @param argumentName -
	 *            the name of the argument that is not used correctly.
	 * @param argumentValue -
	 *            the value of the argument that is not used correctly.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public InvalidArgumentException(String message, String argumentName, Object argumentValue, Throwable cause) {
		super(message, cause);
		this.argumentName = argumentName;
		this.argumentValue = argumentValue;
	}

	/**
	 * Get the argument name.
	 *
	 * @return the name of the argument
	 */
	public String getArgumentName() {
		return argumentName;
	}

	/**
	 * Get the argument value.
	 *
	 * @return the argument value to get
	 */
	public Object getArgumentValue() {
		return argumentValue;
	}
	/**
	 * Returns a String representation of object properties.
	 *
	 * @return String representation of object properties.
	 */
	@Override
	public String toString() {
		String superString = super.toString();
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append(superString);
		builder.append("argumentName", getArgumentName());
		builder.append("argumentValue", getArgumentValue());

		return builder.toString();
	}
}