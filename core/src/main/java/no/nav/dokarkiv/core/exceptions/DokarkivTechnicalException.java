package no.nav.dokarkiv.core.exceptions;

/**
 * The main technical exception type in Joark.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokarkivTechnicalException extends RuntimeException {
	public DokarkivTechnicalException() {
		super();
	}

	public DokarkivTechnicalException(String message) {
		super(message);
	}

	public DokarkivTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public DokarkivTechnicalException(Throwable cause) {
		super(cause);
	}
}
