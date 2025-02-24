package no.nav.dokarkiv.core.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(code = BAD_REQUEST)
public class UgyldigTemakodeException extends DokarkivFunctionalException {
	public UgyldigTemakodeException(String message) {
		super(message);
	}
}
