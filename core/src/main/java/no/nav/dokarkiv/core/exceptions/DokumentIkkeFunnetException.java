package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Kastes når et konkret dokument (selve dokumentet, ikke metadata) ikke finnes
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public final class DokumentIkkeFunnetException extends DokarkivFunctionalException {

	public DokumentIkkeFunnetException(String message) {
		super(message);
	}
}
