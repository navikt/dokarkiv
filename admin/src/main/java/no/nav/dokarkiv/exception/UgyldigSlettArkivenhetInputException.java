package no.nav.dokarkiv.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class UgyldigSlettArkivenhetInputException extends DokarkivFunctionalException {

	public UgyldigSlettArkivenhetInputException(String message) {
		super(message);
	}
}
