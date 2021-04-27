package no.nav.dokarkiv.core.exceptions;

public class InvalidPdfException extends DokarkivFunctionalException {


	public InvalidPdfException(String message) {
		super(message);
	}

	public InvalidPdfException(String message, Throwable cause) {
		super(message, cause);
	}
}
