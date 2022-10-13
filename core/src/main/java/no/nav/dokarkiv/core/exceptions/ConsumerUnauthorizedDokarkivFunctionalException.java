package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ConsumerUnauthorizedDokarkivFunctionalException extends DokarkivFunctionalException {
	public ConsumerUnauthorizedDokarkivFunctionalException(String message) {
		super(message);
	}
}
