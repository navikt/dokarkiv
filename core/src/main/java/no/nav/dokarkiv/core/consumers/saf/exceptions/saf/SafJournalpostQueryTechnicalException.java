package no.nav.dokarkiv.core.consumers.saf.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class SafJournalpostQueryTechnicalException extends DokarkivTechnicalException {
	public SafJournalpostQueryTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
