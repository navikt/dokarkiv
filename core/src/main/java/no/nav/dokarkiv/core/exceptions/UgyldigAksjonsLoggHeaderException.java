package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigAksjonsLoggHeaderException extends Exception {

	public UgyldigAksjonsLoggHeaderException(String message, Throwable cause) {
		super(message, cause);
	}


	public UgyldigAksjonsLoggHeaderException(String message) {
		super(message);
	}
}
