package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class LogiskVedleggIkkeFunnetException extends DokarkivFunctionalException {
	public LogiskVedleggIkkeFunnetException() {
		super();
	}

	public LogiskVedleggIkkeFunnetException(String message) {
		super(message);
	}
}
