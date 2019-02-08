package no.nav.dokarkiv.slettarkivenhet.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class UgyldigInputException extends DokarkivFunctionalException {

	public UgyldigInputException(String message) {
		super(message);
	}
}
