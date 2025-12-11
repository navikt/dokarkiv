package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedForSlettebestillingException extends DokarkivFunctionalException {
	public UnauthorizedForSlettebestillingException(String message) {
		super(message);
	}
}
