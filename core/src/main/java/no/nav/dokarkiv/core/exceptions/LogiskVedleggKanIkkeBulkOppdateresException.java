package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public final class LogiskVedleggKanIkkeBulkOppdateresException extends DokarkivFunctionalException {
	public LogiskVedleggKanIkkeBulkOppdateresException(String message, Throwable e) {
		super(message, e);
	}
}
