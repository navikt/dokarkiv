package no.nav.dokarkiv.core.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

public class SafJournalpostQueryUnauthorizedException extends DokarkivTechnicalException {
	public SafJournalpostQueryUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}
