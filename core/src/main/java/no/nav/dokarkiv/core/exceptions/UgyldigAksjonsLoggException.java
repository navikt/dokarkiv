package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class UgyldigAksjonsLoggException extends RuntimeException {

	public UgyldigAksjonsLoggException(String message, Throwable cause) {
		super(message, cause);
	}


	public UgyldigAksjonsLoggException(String message) {
		super(message);
	}
}
