package no.nav.dokarkiv.inngaaendejournal.v1.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class JournalpostKanIkkeBehandlesException extends DokarkivFunctionalException {
	public JournalpostKanIkkeBehandlesException() {
		super();
	}

	public JournalpostKanIkkeBehandlesException(String message) {
		super(message);
	}
}
