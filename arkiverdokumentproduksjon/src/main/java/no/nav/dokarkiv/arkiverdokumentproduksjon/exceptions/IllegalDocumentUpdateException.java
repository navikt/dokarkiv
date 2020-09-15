package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * Thrown by oppdaterJournal operation when an attempt to add or update
 * documents are made and updates are not allowed.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class IllegalDocumentUpdateException extends DokarkivFunctionalException {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = 5624181226982139323L;

	/**
	 * Constructs a new IllegalDocumentUpdateException.
	 *
	 * @param message The exception message.
	 */
	public IllegalDocumentUpdateException(String message) {
		super(message);
	}

}
