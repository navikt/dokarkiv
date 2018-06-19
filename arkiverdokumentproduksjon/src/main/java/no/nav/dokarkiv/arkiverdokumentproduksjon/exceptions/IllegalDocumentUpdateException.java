package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Thrown by oppdaterJournal operation when an attempt to add or update
 * documents are made and updates are not allowed.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class IllegalDocumentUpdateException extends FunctionalUnrecoverableException {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = ***gammelt_fnr***82139323L;

	/**
	 * Constructs a new IllegalDocumentUpdateException.
	 *
	 * @param message The exception message.
	 */
	public IllegalDocumentUpdateException(String message) {
		super(message);
	}

}
