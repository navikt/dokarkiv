package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ConsumerIsNotSrvSkanMot1408FunctionalException extends DokarkivFunctionalException {
	public ConsumerIsNotSrvSkanMot1408FunctionalException(String message) {
		super(message);
	}
}
