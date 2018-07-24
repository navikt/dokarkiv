package no.nav.dokarkiv.core.exceptions;

/**
 * The main functional exception type in Joark.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokarkivFunctionalException extends RuntimeException {
	public DokarkivFunctionalException() {
		super();
	}

	public DokarkivFunctionalException(String message) {
		super(message);
	}

	public DokarkivFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public DokarkivFunctionalException(Throwable cause) {
		super(cause);
	}
}
