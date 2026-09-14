package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KanIkkeSkjermeDokumentException extends DokarkivFunctionalException {
	public KanIkkeSkjermeDokumentException(String message) {
		super(message);
	}
}
