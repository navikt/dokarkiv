package no.nav.dokarkiv.innsynjournal.v2.exceptions;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class JournalpostIkkeFunnetException extends DokarkivFunctionalException {
	public JournalpostIkkeFunnetException() {
		super();
	}

	public JournalpostIkkeFunnetException(String message) {
		super(message);
	}
}
