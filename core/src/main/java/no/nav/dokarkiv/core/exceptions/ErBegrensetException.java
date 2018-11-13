package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ErBegrensetException extends DokarkivFunctionalException {
	public ErBegrensetException(String message) {
		super(message);
	}
}
