package no.nav.dokarkiv.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigSlettArkivenhetInputException extends DokarkivFunctionalException {

	public UgyldigSlettArkivenhetInputException(String message) {
		super(message);
	}
}
