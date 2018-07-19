package no.nav.dokarkiv.core.repository.ondemand;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Thrown when a search in OnDemand doesn't return a result.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class EmptyOnDemandSearchResultException extends FunctionalUnrecoverableException {

	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***55102798L;

	/**
	 * Constructs a new EmptyOnDemandSearchResultException.
	 *
	 * @param message The exception message.
	 */
	public EmptyOnDemandSearchResultException(String message) {
		super(message);
	}

}
