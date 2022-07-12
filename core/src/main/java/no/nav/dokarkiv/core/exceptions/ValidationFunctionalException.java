package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationFunctionalException extends DokarkivFunctionalException {
	public ValidationFunctionalException(String message) {
		super(message);
	}
}
