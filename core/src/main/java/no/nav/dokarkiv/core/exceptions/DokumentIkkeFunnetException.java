package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class DokumentIkkeFunnetException extends DokarkivFunctionalException {
	public DokumentIkkeFunnetException() {
		super();
	}

	public DokumentIkkeFunnetException(String message) {
		super(message);
	}
}
