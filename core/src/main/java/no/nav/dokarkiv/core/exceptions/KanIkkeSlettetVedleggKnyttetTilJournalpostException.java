package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KanIkkeSlettetVedleggKnyttetTilJournalpostException extends DokarkivFunctionalException {

	public KanIkkeSlettetVedleggKnyttetTilJournalpostException() {
		super();
	}

	public KanIkkeSlettetVedleggKnyttetTilJournalpostException(String message) {
		super(message);
	}

	public KanIkkeSlettetVedleggKnyttetTilJournalpostException(String message, Throwable cause) {
		super(message, cause);
	}

	public KanIkkeSlettetVedleggKnyttetTilJournalpostException(Throwable cause) {
		super(cause);
	}
}
