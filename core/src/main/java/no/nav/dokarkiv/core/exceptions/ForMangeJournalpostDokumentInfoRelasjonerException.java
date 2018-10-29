package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ForMangeJournalpostDokumentInfoRelasjonerException extends DokarkivFunctionalException {
	public ForMangeJournalpostDokumentInfoRelasjonerException(String message) {
		super(message);
	}
}

