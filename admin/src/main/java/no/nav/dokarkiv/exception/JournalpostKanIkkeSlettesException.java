package no.nav.dokarkiv.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@ResponseStatus(NOT_ACCEPTABLE)
public class JournalpostKanIkkeSlettesException extends DokarkivFunctionalException {

	public JournalpostKanIkkeSlettesException(String message) {
		super(message);
	}
}
