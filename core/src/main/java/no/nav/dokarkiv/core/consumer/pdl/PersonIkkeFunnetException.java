package no.nav.dokarkiv.core.consumer.pdl;


import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus (value = HttpStatus.NOT_FOUND)
public class PersonIkkeFunnetException extends DokarkivFunctionalException {
	public PersonIkkeFunnetException(String message) {
		super(message);
	}

	public PersonIkkeFunnetException(Throwable cause, String message) {
		super(message, cause);
	}
}
