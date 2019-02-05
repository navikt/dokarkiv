package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigSkjermArkivenhetHeaderException extends Exception {

	public UgyldigSkjermArkivenhetHeaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public UgyldigSkjermArkivenhetHeaderException(String message) {
		super(message);
	}
}
