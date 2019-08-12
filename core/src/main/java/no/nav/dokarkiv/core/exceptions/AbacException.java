package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a Journalpost or journalpost, document or variantformat that cannot be found.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AbacException extends DokarkivTechnicalException {

	public AbacException(String message) {
		super(message);
	}
}
