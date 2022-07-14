package no.nav.dokarkiv.core.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SafJournalpostUnauthorizedException extends DokarkivTechnicalException {
	public SafJournalpostUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafJournalpostUnauthorizedException(String message) {
		super(message);
	}
}
