package no.nav.dokarkiv.core.consumers.saf.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(value = NOT_FOUND)
public class SafJournalpostIkkeFunnetException extends DokarkivFunctionalException {
	public SafJournalpostIkkeFunnetException(String message) {
		super(message);
	}
}
