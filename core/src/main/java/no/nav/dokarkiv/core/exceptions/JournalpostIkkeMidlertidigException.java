package no.nav.dokarkiv.core.exceptions;

/**
 * @author Leo-Andreas Ervik, Visma Consulting. 24.05.2017.
 */

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class JournalpostIkkeMidlertidigException extends DokarkivFunctionalException {
	
	public JournalpostIkkeMidlertidigException() {
		super();
	}
	
	public JournalpostIkkeMidlertidigException(String message) {
		super(message);
	}
	
}
