package no.nav.dokarkiv.inngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class JournalpostIkkeInngaaendeException extends DokarkivFunctionalException {
	public JournalpostIkkeInngaaendeException() {
		super();
	}

	public JournalpostIkkeInngaaendeException(String message) {
		super(message);
	}
}
