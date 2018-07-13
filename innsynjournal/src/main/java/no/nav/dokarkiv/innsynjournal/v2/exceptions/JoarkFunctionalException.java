package no.nav.dokarkiv.innsynjournal.v2.exceptions;

/**
 * The main functional exception type in Joark.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class JoarkFunctionalException extends RuntimeException {
	public JoarkFunctionalException() {
		super();
	}

	public JoarkFunctionalException(String message) {
		super(message);
	}

	public JoarkFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public JoarkFunctionalException(Throwable cause) {
		super(cause);
	}
}
