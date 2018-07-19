package no.nav.dokarkiv.core.repository.ondemand;

import no.nav.dokarkiv.core.stelvio.SystemUnrecoverableException;

/**
 * Exception class for erros in the OnDemand repository
 *  
 * @author Carl-Henrik Lund
 */
public class OnDemandRepositoryException extends SystemUnrecoverableException {

	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***53346265L;

	/**
	 * Constructs a new OnDemandRepositoryException.
	 * 
	 * @param message The exception message
	 * @param cause The exception cause
	 */
	public OnDemandRepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
