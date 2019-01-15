package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a Journalpost or journalpost, document or variantformat that cannot be found.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DocumentNotFoundException extends DokarkivFunctionalException {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -***gammelt_fnr***76942265L;

	private static final String MESSAGE = "Could not find document";

	/**
	 * Constructs a new DocumentNotFoundException.
	 *
	 * @param cause The root cause.
	 */
	public DocumentNotFoundException(Throwable cause) {
		super(MESSAGE, cause);
	}

	/**
	 * Constructs a new DocumentNotFoundException.
	 *
	 * @param message The exception message
	 * @param cause The root cause.
	 */
	public DocumentNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new DocumentNotFoundException.
	 *
	 * @param message The exception message
	 */
	public DocumentNotFoundException(String message) {
		super(message);
	}
}
