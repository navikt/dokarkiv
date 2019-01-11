package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigAksjonsLoggInfoException extends Exception {

	public UgyldigAksjonsLoggInfoException(String message, Throwable cause) {
		super(message, cause);
	}


	public UgyldigAksjonsLoggInfoException(String message) {
		super(message);
	}
}
