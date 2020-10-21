package no.nav.dokarkiv.core.security.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class MissingClaimException extends RuntimeException {
	public MissingClaimException(String message) {
		super(message);
	}
}
