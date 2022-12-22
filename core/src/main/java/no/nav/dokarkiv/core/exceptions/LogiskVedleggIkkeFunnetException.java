package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class LogiskVedleggIkkeFunnetException extends DokarkivFunctionalException {
	public LogiskVedleggIkkeFunnetException(String message) {
		super(message);
	}

	public LogiskVedleggIkkeFunnetException(String message, Throwable e) {
		super(message, e);
	}
}
