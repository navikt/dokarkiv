package no.nav.dokarkiv.innsynjournal.v2.exceptions;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class UgyldigInputException extends JoarkFunctionalException {
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
