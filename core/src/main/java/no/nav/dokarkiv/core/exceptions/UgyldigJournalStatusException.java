package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST) //TODO: Har vi en bedre statusCode her?
public final class UgyldigJournalStatusException extends DokarkivFunctionalException {
	public UgyldigJournalStatusException() {
		super();
	}

	public UgyldigJournalStatusException(String message) {
		super(message);
	}
}
