package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigHendelseLoggInfoException extends Exception {

	public UgyldigHendelseLoggInfoException(String message, Throwable cause) {
		super(message, cause);
	}


	public UgyldigHendelseLoggInfoException(String message) {
		super(message);
	}
}
