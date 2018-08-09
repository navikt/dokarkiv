package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;

public class DokmotArkivRestFunctionalException extends DokarkivFunctionalException {

	private final HttpStatus httpStatus;

	public DokmotArkivRestFunctionalException(String message, HttpStatus status) {
		super(message);
		this.httpStatus = status;
	}

	public DokmotArkivRestFunctionalException(String message, Throwable cause, HttpStatus status) {
		super(message, cause);
		this.httpStatus = status;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
