package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Samuel Mårten Elmgren, Visma Consulting AS
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class JournalpostDokumentInfoRelasjonIkkeFunnetException extends DokarkivFunctionalException {
	public JournalpostDokumentInfoRelasjonIkkeFunnetException(String message) {
		super(message);
	}
}
