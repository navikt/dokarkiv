package no.nav.dokarkiv.core.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(code = NOT_FOUND)
public final class JournalpostDokumentInfoRelasjonIkkeFunnetException extends DokarkivFunctionalException {
	public JournalpostDokumentInfoRelasjonIkkeFunnetException(String message) {
		super(message);
	}
}
