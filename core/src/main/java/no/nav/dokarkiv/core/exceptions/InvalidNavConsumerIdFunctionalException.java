package no.nav.dokarkiv.core.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class InvalidNavConsumerIdFunctionalException extends DokarkivFunctionalException {
	public InvalidNavConsumerIdFunctionalException(String message) {
		super(message);
	}
}
