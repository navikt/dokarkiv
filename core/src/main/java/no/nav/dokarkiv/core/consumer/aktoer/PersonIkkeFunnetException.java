package no.nav.dokarkiv.core.consumer.aktoer;


import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * Exception PersonIkkeFunnetException.
 *
 * @author Tak Wai Wang (Capgemini)
 */
public class PersonIkkeFunnetException extends DokarkivFunctionalException {
	public PersonIkkeFunnetException(Throwable cause, String message) {
		super(message, cause);
	}
}
