package no.nav.dokarkiv.core.stelvio;

/**
 * This class serves as an uchecked (unrecoverable) wrapper for checked (recoverable) types of exceptions.
 * 
 * OperationalException should be used to wrap an exception when an interface declares it as checked, but the client considers
 * the exception as unrecoverable and wants unchecked exception behaviour.
 * 
 * This wrapper should be preferred over its counterpart <code>ImplementationException</code> when wrapping an exception
 * considered to be operational (outside application boundaries).
 * 
 * @author Christian Kjendseth Wiik (Accenture)
 */
public class OperationalException extends SystemUnrecoverableException {

	private static final long serialVersionUID = ***gammelt_fnr***35567061L;

	private Throwable wrappedException;

	/**
	 * Constructs an <code>OperationalException</code> with message and cause.
	 * 
	 * @param message -
	 *            the exception message.
	 * @param cause -
	 *            the throwable to wrap.
	 */
	public OperationalException(String message, Throwable cause) {
		super(message, cause);
		this.wrappedException = cause;
	}

	/**
	 * Constructs an <code>OperationalException</code> with an exception to wrap.
	 * 
	 * @param exceptionToWrap -
	 *            the throwable to wrap.
	 */
	public OperationalException(Throwable exceptionToWrap) {
		super(exceptionToWrap.getMessage(), exceptionToWrap);
		this.wrappedException = exceptionToWrap;
	}

	/**
	 * Method for retrieving the wrapped <code>Throwable</code>.
	 * 
	 * @return wrappedException the wrapped <code>Throwable</code>.
	 */
	public Throwable getWrappedThrowable() {
		return wrappedException;
	}
}