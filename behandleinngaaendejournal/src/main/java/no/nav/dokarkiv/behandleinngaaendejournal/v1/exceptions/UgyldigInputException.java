package no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

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
