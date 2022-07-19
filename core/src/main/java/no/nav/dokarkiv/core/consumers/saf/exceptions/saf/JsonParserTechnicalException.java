package no.nav.dokarkiv.core.consumers.saf.exceptions.saf;

import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

public class JsonParserTechnicalException extends DokarkivTechnicalException {
	public JsonParserTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
