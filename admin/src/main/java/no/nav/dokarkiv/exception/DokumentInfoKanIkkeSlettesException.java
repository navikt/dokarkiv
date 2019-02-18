package no.nav.dokarkiv.exception;

import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class DokumentInfoKanIkkeSlettesException extends DokarkivFunctionalException {

	public DokumentInfoKanIkkeSlettesException(String message) {
		super(message);
	}
}
