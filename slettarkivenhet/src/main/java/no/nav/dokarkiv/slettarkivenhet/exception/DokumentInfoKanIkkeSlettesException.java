package no.nav.dokarkiv.slettarkivenhet.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentInfoKanIkkeSlettesException extends DokarkivFunctionalException {

	public DokumentInfoKanIkkeSlettesException(String message) {
		super(message);
	}
}
