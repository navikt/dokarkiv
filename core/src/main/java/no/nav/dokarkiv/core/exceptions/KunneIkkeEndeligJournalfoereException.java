package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public final class KunneIkkeEndeligJournalfoereException extends DokarkivFunctionalException {
	public KunneIkkeEndeligJournalfoereException() {
		super();
	}

	public KunneIkkeEndeligJournalfoereException(String message) {
		super(message);
	}
}
