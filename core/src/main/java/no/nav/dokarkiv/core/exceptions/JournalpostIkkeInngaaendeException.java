package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public final class JournalpostIkkeInngaaendeException extends DokarkivFunctionalException {
	public JournalpostIkkeInngaaendeException() {
		super();
	}

	public JournalpostIkkeInngaaendeException(String message) {
		super(message);
	}
}
