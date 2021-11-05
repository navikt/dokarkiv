package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(code = NOT_FOUND)
public class PersonIngenIdentFunnetException extends DokarkivFunctionalException {
	public PersonIngenIdentFunnetException(String message) {
		super(message);
	}
}
