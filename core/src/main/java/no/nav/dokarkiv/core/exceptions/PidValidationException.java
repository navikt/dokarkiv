package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;
import no.nav.dokarkiv.core.stelvio.Pid;

/**
 * Exception thrown as a result of Pid validation failure.
 *
 * @author Morten Andersen-Gott, Accenture
 *
 * @see Pid
 */
public class PidValidationException extends FunctionalUnrecoverableException {

	private static final long serialVersionUID = ***gammelt_fnr***82768191L;

	/**
	 * Constructs a <code>PidValidationException</code> with message and cause.
	 *
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable that caused the exception to be raised.
	 */
	public PidValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>PidValidationException</code> with pid.
	 *
	 * @param pid -
	 *            the pid that did not pass validation.
	 *
	 */
	public PidValidationException(String pid) {
		super("Pid validation failed, " + pid + " is not a valid personal identification number");
	}

}