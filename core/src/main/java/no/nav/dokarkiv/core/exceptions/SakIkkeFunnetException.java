package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public final class SakIkkeFunnetException extends DokarkivFunctionalException {

	public SakIkkeFunnetException(String message) {
		super(message);
	}

}
