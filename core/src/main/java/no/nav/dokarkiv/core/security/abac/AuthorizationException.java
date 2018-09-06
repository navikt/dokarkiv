package no.nav.dokarkiv.core.security.abac;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AuthorizationException extends RuntimeException {

	public AuthorizationException(String message) {
		super(message);
	}
}
