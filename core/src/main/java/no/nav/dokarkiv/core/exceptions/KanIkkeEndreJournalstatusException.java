package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class KanIkkeEndreJournalstatusException extends DokarkivFunctionalException {

	public KanIkkeEndreJournalstatusException(String message) {
		super(message);
	}
}
