package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SlettebestillingIkkeFunnetException extends DokarkivFunctionalException {
	public SlettebestillingIkkeFunnetException(String message) {
		super(message);
	}
}
