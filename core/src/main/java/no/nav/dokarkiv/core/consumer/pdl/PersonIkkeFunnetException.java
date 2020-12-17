package no.nav.dokarkiv.core.consumer.pdl;


import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * Exception PersonIkkeFunnetException.
 *
 * @author Tak Wai Wang (Capgemini)
 */
public class PersonIkkeFunnetException extends DokarkivFunctionalException {
	public PersonIkkeFunnetException(String message) {
		super(message);
	}

	public PersonIkkeFunnetException(Throwable cause, String message) {
		super(message, cause);
	}
}
