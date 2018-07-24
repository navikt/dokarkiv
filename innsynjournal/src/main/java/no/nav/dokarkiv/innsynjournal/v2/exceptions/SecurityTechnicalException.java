package no.nav.dokarkiv.innsynjournal.v2.exceptions;

/**
 * @author Torgeir Cook, Visma Consulting.
 *
 * Thrown if technical faults in innsyn logic
 *
 */
public class SecurityTechnicalException extends RuntimeException {

	public SecurityTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
