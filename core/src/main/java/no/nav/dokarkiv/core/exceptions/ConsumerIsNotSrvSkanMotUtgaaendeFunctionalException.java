package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ConsumerIsNotSrvSkanMotUtgaaendeFunctionalException extends DokarkivFunctionalException {
	public ConsumerIsNotSrvSkanMotUtgaaendeFunctionalException(String message) {
		super(message);
	}
}
