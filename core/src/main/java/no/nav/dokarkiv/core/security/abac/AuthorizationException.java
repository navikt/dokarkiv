package no.nav.dokarkiv.core.security.abac;

public class AuthorizationException extends RuntimeException {

	public AuthorizationException(String message) {
		super(message);
	}
}
