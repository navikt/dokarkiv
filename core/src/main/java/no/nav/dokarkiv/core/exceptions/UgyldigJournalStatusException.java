package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED)
public final class UgyldigJournalStatusException extends DokarkivFunctionalException {
	public UgyldigJournalStatusException() {
		super();
	}

	public UgyldigJournalStatusException(String message) {
		super(message);
	}
}
