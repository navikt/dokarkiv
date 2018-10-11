package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class JournalpostIkkeFunnetException extends DokarkivFunctionalException {
	public JournalpostIkkeFunnetException() {
		super();
	}

	public JournalpostIkkeFunnetException(String message) {
		super(message);
	}
}
