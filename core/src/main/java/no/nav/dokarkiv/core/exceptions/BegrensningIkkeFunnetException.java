package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BegrensningIkkeFunnetException extends DokarkivFunctionalException {

	public BegrensningIkkeFunnetException(String message) {
		super(message);
	}
}
