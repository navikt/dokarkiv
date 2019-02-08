package no.nav.dokarkiv.slettarkivenhet.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalpostKanIkkeSlettesException extends DokarkivFunctionalException {

	public JournalpostKanIkkeSlettesException(String message) {
		super(message);
	}
}
