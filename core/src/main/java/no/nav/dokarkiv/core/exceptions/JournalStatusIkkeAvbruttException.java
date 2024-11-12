package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public final class JournalStatusIkkeAvbruttException extends DokarkivFunctionalException {
	public JournalStatusIkkeAvbruttException() {
		super();
	}

	public JournalStatusIkkeAvbruttException(String message) {
		super(message);
	}
}
