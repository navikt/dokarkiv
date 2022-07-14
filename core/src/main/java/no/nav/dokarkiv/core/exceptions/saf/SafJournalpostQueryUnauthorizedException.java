package no.nav.dokarkiv.core.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class SafJournalpostQueryUnauthorizedException extends DokarkivTechnicalException {
	public SafJournalpostQueryUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
