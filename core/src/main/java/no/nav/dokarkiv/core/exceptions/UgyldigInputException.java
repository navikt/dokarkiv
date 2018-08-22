package no.nav.dokarkiv.core.exceptions;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class UgyldigInputException extends DokarkivFunctionalException {
	public UgyldigInputException() {
		super();
	}

	public UgyldigInputException(String message) {
		super(message);
	}

	public UgyldigInputException(String message, Throwable cause) {
		super(message, cause);
	}
}
