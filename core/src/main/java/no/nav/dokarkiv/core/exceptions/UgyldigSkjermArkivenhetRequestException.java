package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigSkjermArkivenhetRequestException extends Exception {

	public UgyldigSkjermArkivenhetRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public UgyldigSkjermArkivenhetRequestException(String message) {
		super(message);
	}
}
