package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SlettIkkeVedleggTilknyttJournalpostException extends DokarkivFunctionalException {

	public SlettIkkeVedleggTilknyttJournalpostException() {
		super();
	}

	public SlettIkkeVedleggTilknyttJournalpostException(String message) {
		super(message);
	}

	public SlettIkkeVedleggTilknyttJournalpostException(String message, Throwable cause) {
		super(message, cause);
	}

	public SlettIkkeVedleggTilknyttJournalpostException(Throwable cause) {
		super(cause);
	}
}
